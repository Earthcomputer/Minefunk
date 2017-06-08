package net.earthcomputer.minefunk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.earthcomputer.minefunk.parser.ASTProcessor;
import net.earthcomputer.minefunk.parser.ASTRoot;
import net.earthcomputer.minefunk.parser.CallGraphVisitor;
import net.earthcomputer.minefunk.parser.Index;
import net.earthcomputer.minefunk.parser.MinefunkParser;
import net.earthcomputer.minefunk.parser.MinefunkParserConstants;
import net.earthcomputer.minefunk.parser.ParseException;
import net.earthcomputer.minefunk.parser.Token;

/**
 * The main class of the compiler
 * 
 * @author Earthcomputer
 */
public class Main {

	/**
	 * The directory that the program is being run from
	 */
	private static Path workingDirectory = new File(".").getAbsoluteFile().toPath();
	/**
	 * The command line options
	 */
	private static CommandLineOptions cmdLineOptions;

	public static void main(String[] args) throws IOException {
		// Parse command line options
		cmdLineOptions = CommandLineOptions.parse(workingDirectory, args);
		if (cmdLineOptions == null) {
			System.err.println(CommandLineOptions.USAGE);
			return;
		}

		// Get matching files
		List<File> inputFiles = new ArrayList<>();
		Files.walkFileTree(workingDirectory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (cmdLineOptions.getInputFileMatcher().accept(file.toFile())) {
					inputFiles.add(file.toFile());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
				System.err.println("Failed to read file " + file);
				e.printStackTrace();
				return FileVisitResult.CONTINUE;
			}
		});

		Map<String, ASTRoot> asts = new LinkedHashMap<>(inputFiles.size());
		Index index = new Index();
		Map<String, List<ParseException>> exceptions = new LinkedHashMap<>();

		// Parse asts from all input files
		for (File inputFile : inputFiles) {
			String filename = workingDirectory.relativize(inputFile.toPath()).toString();
			exceptions.put(filename, new ArrayList<>());
			InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
			ASTRoot root;
			try {
				root = new MinefunkParser(in).parse();
			} catch (ParseException e) {
				exceptions.get(filename).add(e);
				continue;
			} finally {
				in.close();
			}
			asts.put(filename, root);
		}
		addStdLib(asts, exceptions);
		if (handleExceptions("parsing", exceptions)) {
			return;
		}

		// Pre-index check
		asts.forEach((filename, root) -> {
			ASTProcessor.preIndexCheck(root, exceptions.get(filename));
		});
		if (handleExceptions("pre-index check", exceptions)) {
			return;
		}

		// Indexing
		asts.forEach((filename, root) -> {
			ASTProcessor.index(root, index, exceptions.get(filename));
		});
		if (handleExceptions("indexing", exceptions)) {
			return;
		}

		// Resolve functions after indexing
		List<ParseException> globalExceptions = new ArrayList<>();
		index.resolvePendingFunctions(globalExceptions);
		if (handleExceptions("resolve functions", Collections.singletonMap("global", globalExceptions))) {
			return;
		}

		// Post-index check
		asts.forEach((filename, root) -> {
			ASTProcessor.postIndexCheck(root, index, exceptions.get(filename));
		});
		if (handleExceptions("post-index check", exceptions)) {
			return;
		}

		// Check circular references
		Map<CallGraphVisitor.CallGraphNode, Set<CallGraphVisitor.CallGraphNode>> callGraph = new HashMap<>();
		asts.forEach((filename, root) -> {
			ASTProcessor.addToCallGraph(callGraph, root, index, exceptions.get(filename));
		});
		CallGraphAnalyzer.StronglyConnectedComponentsFinder.Result<CallGraphVisitor.CallGraphNode> cycleSearchResults = new CallGraphAnalyzer.StronglyConnectedComponentsFinder<>(
				callGraph).findStronglyConnectedComponents();
		asts.forEach((filename, root) -> {
			ASTProcessor.checkForCyclicReferences(cycleSearchResults, root, index, exceptions.get(filename));
		});
		if (handleExceptions("circular references check", exceptions)) {
			return;
		}

		// Command generation
		Map<String, List<String>> commandLists = new HashMap<>();
		asts.forEach((filename, root) -> {
			ASTProcessor.generateCommandLists(root, index, commandLists, exceptions.get(filename));
		});
		if (handleExceptions("command generation", exceptions)) {
			return;
		}

		// Output commands generated
		commandLists.forEach((funcId, commands) -> {
			File outputFile = new File(cmdLineOptions.getOutputDirectory(), funcId.replace(':', '/') + ".mcfunction");
			outputFile.getParentFile().mkdirs();
			try {
				Files.write(outputFile.toPath(), commands, StandardOpenOption.CREATE);
			} catch (IOException e) {
				System.err.println("Failed to write function " + funcId + ", " + e);
			}
		});
	}

	/**
	 * Adds the standard library to the list of ASTs
	 * 
	 * @param asts
	 *            - the list of ASTs
	 * @param exceptions
	 *            - the compiler errors to add to
	 */
	private static void addStdLib(Map<String, ASTRoot> asts, Map<String, List<ParseException>> exceptions) {
		try {
			URL jarLocation = Main.class.getResource("/" + Main.class.getName().replace('.', '/') + ".class");
			if (jarLocation.getProtocol().contains("jar")) {
				File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				JarFile jar = new JarFile(jarFile);
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (entry.getName().startsWith("stdlib/") && entry.getName().endsWith(".funk")) {
						exceptions.put(entry.getName(), new ArrayList<>());
						try {
							asts.put(entry.getName(), new MinefunkParser(jar.getInputStream(entry)).parse());
						} catch (ParseException e) {
							exceptions.get(entry.getName()).add(e);
						}
					}
				}
				jar.close();
			} else {
				Path path = Paths.get(Main.class.getResource("/stdlib").toURI());
				Path parent = path.getParent();
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
						if (!path.toString().endsWith(".funk")) {
							return FileVisitResult.CONTINUE;
						}
						String filename = parent.relativize(path).toString();
						exceptions.put(filename, new ArrayList<>());
						try {
							asts.put(filename, new MinefunkParser(Files.newBufferedReader(path)).parse());
						} catch (ParseException e) {
							exceptions.get(filename).add(e);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
						e.printStackTrace();
						return FileVisitResult.TERMINATE;
					}
				});
			}
		} catch (IOException | URISyntaxException e) {
			System.err.println("Unable to read stdlib");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Handles any compiler errors that occur in each phase
	 * 
	 * @param phase
	 *            - the compilation phase to output to the user
	 * @param exceptions
	 *            - the map of parse exceptions that occurred for each file
	 * @return Whether there were any compiler errors during this phase
	 */
	private static boolean handleExceptions(String phase, Map<String, List<ParseException>> exceptions) {
		// Check if there were any errors at all
		int errorCount = exceptions.values().stream().mapToInt(List::size).sum();
		if (errorCount == 0) {
			return false;
		}

		// Summarizing message
		System.err.printf("Encountered %d errors during phase %s:\n", errorCount, phase);

		// Loop through the errors in each file
		exceptions.forEach((filename, errorsInFile) -> {
			// If there were no errors in this file, skip it.
			if (errorsInFile.isEmpty()) {
				return;
			}

			// Read lines from the file so we can helpfully echo them to the
			// user
			List<String> fileLines;
			try {
				fileLines = Files.readAllLines(workingDirectory.resolve(filename));
			} catch (IOException e) {
				fileLines = Collections.emptyList();
			}
			final List<String> fileLines_f = fileLines;

			errPrintDivider();

			// Print the name of the file
			System.err.println("In file " + filename + ":");
			// Print all the errors in the file
			errorsInFile.forEach(ex -> errOutputParseException(fileLines_f, ex));
		});

		errPrintDivider();

		// Print a witty comment to brighten up the user's day
		errPrintWittyComment();

		// Print stack traces
		if (cmdLineOptions.showStacktrace()) {
			errPrintDivider();
			System.err.println("In case any of these were errors in the compiler itself,");
			System.err.println("here are the stack traces:");
			exceptions.forEach((filename, errorsInFile) -> {
				errorsInFile.forEach(ex -> {
					errPrintDivider();
					ex.printStackTrace();
				});
			});
		}

		return true;
	}

	/**
	 * Outputs a compiler error in a user-friendly way
	 * 
	 * @param fileLines
	 *            - the lines read from the errored file
	 * @param ex
	 *            - the compiler error
	 */
	private static void errOutputParseException(List<String> fileLines, ParseException ex) {
		if (ex.expectedTokenSequences != null) {
			errPrintSyntaxError(fileLines, ex);
		} else {
			errPrintNonSyntaxError(fileLines, ex);
		}
		System.err.println();
	}

	/**
	 * Outputs a syntax error in a user-friendly way
	 * 
	 * @param fileLines
	 *            - the lines read from the errored file
	 * @param ex
	 *            - the syntax error
	 */
	private static void errPrintSyntaxError(List<String> fileLines, ParseException ex) {
		// Current token is the one that's OK, the errored token is the next one
		Token errTok = ex.currentToken.next;

		// Echo the errored token
		errCopyLineFromFile(fileLines, errTok.beginLine, errTok.beginColumn, errTok.endLine, errTok.endColumn);

		// An informative message as to why it's a syntax error
		System.err.printf("From %d:%d to %d:%d... Token \"%s\" encountered, but was not expected in this location.\n",
				errTok.beginLine, errTok.beginColumn, errTok.endLine, errTok.endColumn, errTok.image);
		StringBuilder expected = new StringBuilder("\tExpected one of the following sequences instead:\n");
		for (int[] sequence : ex.expectedTokenSequences) {
			expected.append("\t\t|");
			for (int tokKind : sequence) {
				expected.append(" ");
				expected.append(MinefunkParserConstants.tokenImage[tokKind]);
			}
			expected.append("\n");
		}
		System.err.print(expected);
	}

	/**
	 * Outputs a compiler error that's not a syntax error in a user-friendly way
	 * 
	 * @param fileLines
	 *            - the lines read from the errored file
	 * @param ex
	 *            - the compiler error
	 */
	private static void errPrintNonSyntaxError(List<String> fileLines, ParseException ex) {
		Token tok = ex.currentToken;
		// Echo the errored code from the file
		errCopyLineFromFile(fileLines, tok.beginLine, tok.beginColumn, tok.endLine, tok.endColumn);
		// Description as to why it's an error
		System.err.printf("From %d:%d to %d:%d... %s\n", tok.beginLine, tok.beginColumn, tok.endLine, tok.endColumn,
				ex.getMessage());
	}

	/**
	 * Echos a region of a file to the console and highlights it
	 * 
	 * @param fileLines
	 * @param beginLine
	 * @param beginColumn
	 * @param endLine
	 * @param endColumn
	 */
	private static void errCopyLineFromFile(List<String> fileLines, int beginLine, int beginColumn, int endLine,
			int endColumn) {
		// Can't echo lines that aren't there
		if (endLine > fileLines.size()) {
			System.err.println("[Unable to read from file]");
			return;
		}

		/*
		 * The following code accounts for the following situations. Errored
		 * code is denoted as capital letters. All other characters are
		 * non-errors.
		 */
		// @formatter:off
		
		// 1. Single character
		// Input: hello World!
		// Output:
		// hello World!
		//       ^
		// 2. Single line
		// Input: hello WORLD!
		// Output:
		// hello WORLD!
		//       ^---^
		// 3. Two lines
		// Input:
		// heLLO
		// WORld!
		// Output:
		// heLLO
		//   ^--
		// WORld!
		// --^
		// 4. Many lines
		// Input:
		// heLL
		// OWO
		// RLD!
		// Output:
		// heLL
		//   ^-
		// [...]
		// RLD!
		// --^
		// @formatter:on

		// Always print first line
		String line = fileLines.get(beginLine - 1);
		System.err.println(line);
		// Print whitespace up to the start of the error
		for (int i = 0; i < beginColumn - 1; i++) {
			System.err.print(Util.charToWhitespace(line.charAt(i)));
		}
		// If beginLine and endLine are equal, we have case 1 or 2. Otherwise,
		// we have case 3 or 4
		if (beginLine == endLine) {
			// Print a ^ at the start of the error
			System.err.print('^');
			// Print -s between the start and end of the error
			for (int i = beginColumn + 1; i < endColumn; i++) {
				System.err.print('-');
			}
			// In case 2, print a ^ at the end of the error. If this was done in
			// case 1, we'd have a repeat ^
			if (beginColumn != endColumn) {
				System.err.print('^');
			}
			System.err.println();
		} else {
			// Print a ^ at the start of the error
			System.err.print('^');
			// Print -s until the end of the line
			for (int i = beginColumn + 1; i < line.length(); i++) {
				System.err.print('-');
			}
			System.err.println();
			// In case 4, we print a [...]
			if (endLine > beginLine + 1) {
				System.err.println("[...]");
			}
			// Echo the last line from the file
			line = fileLines.get(endLine - 1);
			System.err.println(line);
			// Print -s until the end of the error
			for (int i = 0; i < endColumn; i++) {
				System.err.print('-');
			}
			// Print a ^ at the end of the error
			System.err.print('^');
			System.err.println();
		}
	}

	/**
	 * Prints a witty comment to brighten up the user's day
	 */
	private static void errPrintWittyComment() {
		// Date-specific witty comments
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int[] easter = Util.calculateEaster(year);

		if (month == Calendar.DECEMBER && day == 25) {
			// Christmas
			System.err.println("// Apart from this, I hope Christmas is going well!");
		} else if (month == Calendar.OCTOBER && day == 31) {
			// Halloween
			System.err.println("// Put a pumpkin on your head, you'd look better with it");
		} else if (month == Calendar.JANUARY && day == 1) {
			// New Year's Day
			System.err.println("// GREAT way to start a year!");
		} else if (month == Calendar.APRIL && day == 1 && cal.get(Calendar.HOUR_OF_DAY) < 12) {
			// April Fools
			System.err.println("// April fools!");
		} else if (month == Calendar.FEBRUARY && day == 29) {
			// Leap Day
			System.err.println("// I hope this isn't your birthday. Just sayin'");
		} else if (month == Calendar.DECEMBER && day == 18) {
			// Earthcomputer's Birthday
			System.err.println("// A gift for Earthcomputer's birthday");
		} else if (month == easter[0] && day == easter[1]) {
			// Easter
			System.err.println("// Your Easter egg has hatched!");
		}

		// Print a random witty comment
		String[] wittyComments = { "You're a map-maker! Make maps then, not mistakes!", "Oops!",
				"It's not you, it's me...", "Now for the hard part: the fix...",
				"Maybe if you just stopped making mistakes", "Man 0-1 Machine", "Okay, whatever",
				"Perhaps it would be better if you just went back to playing Minecraft", "No! What happened?!",
				"Earthcomputer has nice hair", "Minefunk left the game", "My favourite block is the command block",
				"My favourite item is pufferfish", "My favourite mob is the vex", "kill @a[name=!Earthcomputer]",
				"Awkwardness is happening!", "Map-making is harder than circles in Minecraft" };
		System.err.println("// " + wittyComments[Math.abs((int) System.nanoTime()) % wittyComments.length]);
	}

	/**
	 * Prints a divider (lots of -s)
	 */
	private static void errPrintDivider() {
		System.err.println("--------------------------------");
	}

}
