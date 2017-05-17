package net.earthcomputer.minefunk;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class FileMatcher implements FileFilter {

	private Path directory;
	private Pattern pattern;

	public FileMatcher(Path directory, String pattern) {
		this.directory = directory;
		this.pattern = compilePattern(pattern);
		if (this.pattern == null) {
			throw new IllegalArgumentException("Invalid pattern: " + pattern);
		}
	}

	@Override
	public boolean accept(File file) {
		return pattern.matcher(directory.relativize(file.toPath()).toString()).matches();
	}

	private static Pattern compilePattern(String pattern) {
		pattern = pattern.replace("/", File.separator);
		pattern = pattern.replace(":", File.pathSeparator);
		if (pattern.contains("***")) {
			return null;
		}
		if (pattern.contains("?")) {
			return null;
		}
		pattern = pattern.replace("\\", "\\\\");
		pattern = pattern.replace(".", "\\.");
		pattern = pattern.replace("**", ".?");
		pattern = pattern.replace("*", "[^" + File.separator.replace("\\", "\\\\") + "]*");
		pattern = pattern.replace("?", "*");
		String[] parts = pattern.split(File.pathSeparator);
		boolean first = true;
		pattern = "";
		for (String part : parts) {
			if (!first) {
				pattern += "|";
			}
			pattern += "(" + part + ")";
			first = false;
		}
		return Pattern.compile(pattern);
	}

}