package net.earthcomputer.minefunk;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class representing command line options
 * 
 * @author Earthcomputer
 */
public class CommandLineOptions {

	// @formatter:off
	public static final String USAGE = "java -jar minefunk.jar <source-files> [options ...]\n"
			+ "Options are:\n"
			+ "--output: The output directory\n"
			+ "--stacktace: Whether to show the stacktrace of a compiler error (debug feature)\n";
	// @formatter:on

	private FileMatcher inputFileMatcher;
	private File outputDirectory;
	private boolean showStacktrace;

	private CommandLineOptions() {
	}

	/**
	 * Parses the command line options into an instance of this class
	 * 
	 * @param workingDirectory
	 *            - the working directory
	 * @param args
	 *            - the raw command line args
	 * @return An instance of this class representing these command line
	 *         options, or <tt>null</tt> if an error occurred in parsing
	 */
	public static CommandLineOptions parse(Path workingDirectory, String[] args) {
		List<String> argsList = new ArrayList<>(args.length);
		for (String arg : args) {
			argsList.add(arg);
		}
		return parse(workingDirectory, argsList);
	}

	/**
	 * Same as {@link #parse(Path, String[])}, except uses a list which it
	 * modifies
	 * 
	 * @param workingDirectory
	 *            - the working directory
	 * @param args
	 *            - the raw command line args
	 * @return An instance of this class representing these command line options
	 */
	private static CommandLineOptions parse(Path workingDirectory, List<String> args) {
		CommandLineOptions opts = new CommandLineOptions();
		String opt;
		opts.showStacktrace = findFlag(args, "--stacktrace") | findFlag(args, "-s");
		opt = findStringOption(args, "--output", "-o");
		if (opt == null) {
			opts.outputDirectory = workingDirectory.toFile();
		} else {
			opts.outputDirectory = new File(opt);
		}

		if (args.isEmpty()) {
			return null;
		}
		opts.inputFileMatcher = new FileMatcher(workingDirectory, args.toArray(new String[args.size()]));

		return opts;
	}

	/**
	 * Removes all occurrences of the given flag in <tt>args</tt> and returns
	 * whether any were found
	 * 
	 * @param args
	 *            - the command line arguments
	 * @param flag
	 *            - the flag to find
	 * @return Whether the flag was found
	 */
	private static boolean findFlag(List<String> args, String flag) {
		boolean found = false;
		Iterator<String> argItr = args.iterator();
		while (argItr.hasNext()) {
			if (flag.equals(argItr.next())) {
				found = true;
				argItr.remove();
			}
		}
		return found;
	}

	/**
	 * Removes all occurrences of the given string option in <tt>args</tt> and
	 * returns the value found, or <tt>null</tt> if not found
	 * 
	 * @param args
	 *            - the command line arguments
	 * @param option
	 *            - the option to find
	 * @param shorthand
	 *            - the shorthand alias of the option
	 * @return The value of the option
	 */
	private static String findStringOption(List<String> args, String option, String shorthand) {
		String value = null;
		Iterator<String> argItr = args.iterator();
		while (argItr.hasNext()) {
			String tmp = argItr.next();
			if (option.equals(tmp) || shorthand.equals(tmp)) {
				argItr.remove();
				if (argItr.hasNext()) {
					tmp = argItr.next();
					if (value == null) {
						value = tmp;
					}
					argItr.remove();
				}
			}
		}
		return value;
	}

	/**
	 * Gets the input file matcher
	 * 
	 * @return The input file matcher
	 */
	public FileMatcher getInputFileMatcher() {
		return inputFileMatcher;
	}

	/**
	 * Gets the output directory
	 * 
	 * @return The output directory
	 */
	public File getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * Gets whether the command line flags indicate that we should show the
	 * stack trace of a compiler error
	 * 
	 * @return Whether the show stacktace flag was set
	 */
	public boolean showStacktrace() {
		return showStacktrace;
	}

}
