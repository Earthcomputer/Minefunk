package net.earthcomputer.minefunk;

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
			+ "--show-stacktace: Whether to show the stacktrace of a compiler error (debug feature)\n";
	// @formatter:on

	private FileMatcher inputFileMatcher;
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
		opts.showStacktrace = findFlag(args, "--stacktrace");

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
	 * Gets the input file matcher
	 * 
	 * @return The input file matcher
	 */
	public FileMatcher getInputFileMatcher() {
		return inputFileMatcher;
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
