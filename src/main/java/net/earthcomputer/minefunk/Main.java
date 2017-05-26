package net.earthcomputer.minefunk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.earthcomputer.minefunk.parser.ASTProcessor;
import net.earthcomputer.minefunk.parser.ASTRoot;
import net.earthcomputer.minefunk.parser.Index;
import net.earthcomputer.minefunk.parser.MinefunkParser;
import net.earthcomputer.minefunk.parser.MinefunkParserConstants;
import net.earthcomputer.minefunk.parser.ParseException;
import net.earthcomputer.minefunk.parser.Token;

public class Main {

	private static Path workingDirectory = new File(".").getAbsoluteFile().toPath();

	private static void printUsage() {
		System.out.println("java -jar minefunk.jar <source-files>");
	}

	public static void main(String[] args) throws ParseException, IOException {
		if (args.length == 0) {
			printUsage();
			return;
		}
		FileMatcher fileMatcher = new FileMatcher(workingDirectory, args[0]);
		List<File> inputFiles = new ArrayList<>();
		Files.walkFileTree(workingDirectory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (fileMatcher.accept(file.toFile())) {
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
		if (handleExceptions("parsing", exceptions)) {
			return;
		}

		asts.forEach((filename, root) -> {
			ASTProcessor.preIndexCheck(root, exceptions.get(filename));
		});
		if (handleExceptions("pre-index check", exceptions)) {
			return;
		}

		asts.forEach((filename, root) -> {
			ASTProcessor.index(root, index, exceptions.get(filename));
		});
		if (handleExceptions("indexing", exceptions)) {
			return;
		}

		List<ParseException> globalExceptions = new ArrayList<>();
		index.resolvePendingFunctions(globalExceptions);
		if (handleExceptions("resolve functions", Collections.singletonMap("global", globalExceptions))) {
			return;
		}

		asts.forEach((filename, root) -> {
			ASTProcessor.postIndexCheck(root, index, exceptions.get(filename));
		});
		if (handleExceptions("post-index check", exceptions)) {
			return;
		}

		Map<String, List<String>> commandLists = new HashMap<>();
		asts.forEach((filename, root) -> {
			ASTProcessor.generateCommandLists(root, index, commandLists, exceptions.get(filename));
		});
		if (handleExceptions("command generation", exceptions)) {
			return;
		}

		commandLists.forEach((funcId, commands) -> {
			System.out.println(funcId);
			commands.forEach(command -> {
				System.out.println("  " + command);
			});
		});
	}

	private static boolean handleExceptions(String phase, Map<String, List<ParseException>> exceptions) {
		int errorCount = exceptions.values().stream().mapToInt(List::size).sum();
		if (errorCount != 0) {
			System.err.printf("Encountered %d errors during phase %s:\n", errorCount, phase);
			exceptions.forEach((filename, errorsInFile) -> {
				if (!errorsInFile.isEmpty()) {
					List<String> fileLines;
					try {
						fileLines = Files.readAllLines(workingDirectory.resolve(filename));
					} catch (IOException e) {
						fileLines = Collections.emptyList();
					}
					final List<String> fileLines_f = fileLines;
					errPrintDivider();
					System.err.println("In file " + filename + ":");
					errorsInFile.forEach(ex -> errOutputParseException(fileLines_f, ex));
				}
			});
			errPrintDivider();
			errPrintWittyComment();
			errPrintDivider();
			System.err.println("In case any of these were errors in the compiler itself,");
			System.err.println("here are the stack traces:");
			exceptions.forEach((filename, errorsInFile) -> {
				errorsInFile.forEach(ex -> {
					errPrintDivider();
					ex.printStackTrace();
				});
			});
			return true;
		} else {
			return false;
		}
	}

	private static void errOutputParseException(List<String> fileLines, ParseException ex) {
		if (ex.expectedTokenSequences != null) {
			errPrintSyntaxError(fileLines, ex);
		} else {
			errPrintNonSyntaxError(fileLines, ex);
		}
		System.err.println();
	}

	private static void errPrintSyntaxError(List<String> fileLines, ParseException ex) {
		Token tok = ex.currentToken.next;
		errCopyLineFromFile(fileLines, tok.beginLine, tok.beginColumn, tok.endLine, tok.endColumn);
		System.err.printf("From %d:%d to %d:%d... Token \"%s\" encountered, but was not expected in this location.\n",
				tok.beginLine, tok.beginColumn, tok.endLine, tok.endColumn, tok.image);
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

	private static void errPrintNonSyntaxError(List<String> fileLines, ParseException ex) {
		Token tok = ex.currentToken;
		errCopyLineFromFile(fileLines, tok.beginLine, tok.beginColumn, tok.endLine, tok.endColumn);
		System.err.printf("From %d:%d to %d:%d... %s\n", tok.beginLine, tok.beginColumn, tok.endLine, tok.endColumn,
				ex.getMessage());
	}

	private static void errCopyLineFromFile(List<String> fileLines, int beginLine, int beginColumn, int endLine,
			int endColumn) {
		if (endLine > fileLines.size()) {
			System.err.println("[Unable to read from file]");
			return;
		}

		String line = fileLines.get(beginLine - 1);
		System.err.println(line);
		for (int i = 0; i < beginColumn - 1; i++) {
			System.err.print(Util.charToWhitespace(line.charAt(i)));
		}
		if (beginLine == endLine) {
			System.err.print('^');
			for (int i = beginColumn + 1; i < endColumn; i++) {
				System.err.print('-');
			}
			if (beginColumn != endColumn) {
				System.err.print('^');
			}
			System.err.println();
		} else {
			System.err.print('^');
			for (int i = beginColumn + 1; i < line.length(); i++) {
				System.err.print('-');
			}
			System.err.println();
			if (endLine > beginLine + 1) {
				System.err.println("[...]");
			}
			line = fileLines.get(endLine - 1);
			System.err.println(line);
			for (int i = 0; i < endColumn; i++) {
				System.err.print('-');
			}
			System.err.print('^');
			System.err.println();
		}
	}

	private static void errPrintWittyComment() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (month == Calendar.DECEMBER && day == 25) {
			System.err.println("// Apart from this, I hope Christmas is going well!");
		} else if (month == Calendar.OCTOBER && day == 31) {
			System.err.println("// Put a pumpkin on your head, you'd look better with it");
		} else if (month == Calendar.JANUARY && day == 1) {
			System.err.println("// GREAT way to start a year!");
		} else if (month == Calendar.APRIL && day == 1 && cal.get(Calendar.HOUR_OF_DAY) < 12) {
			System.err.println("// April fools!");
		} else if (month == Calendar.FEBRUARY && day == 29) {
			System.err.println("// I hope this isn't your birthday. Just sayin'");
		} else if (month == Calendar.DECEMBER && day == 18) {
			System.err.println("// A gift for Earthcomputer's birthday");
		}
		String[] wittyComments = { "You're a map-maker! Make maps then, not mistakes!", "Oops!",
				"It's not you, it's me...", "Now for the hard part: the fix...",
				"Maybe if you just stopped making mistakes", "Man 0-1 Machine", "Okay, whatever",
				"Perhaps it would be better if you just went back to playing Minecraft", "No! What happened?!",
				"Earthcomputer has nice hair", "Minefunk left the game", "My favourite block is the command block",
				"My favourite item is pufferfish", "My favourite mob is the vex", "kill @a[name=!Earthcomputer]",
				"Awkwardness is happening!", "Map-making is harder than circles in Minecraft" };
		System.err.println("// " + wittyComments[Math.abs((int) System.nanoTime()) % wittyComments.length]);
	}

	private static void errPrintDivider() {
		System.err.println("--------------------------------");
	}

}
