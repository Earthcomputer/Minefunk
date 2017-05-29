package net.earthcomputer.minefunk;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Matches files based on the input pattern
 * 
 * @author Earthcomputer
 */
public class FileMatcher implements FileFilter {

	private Path directory;
	private Pattern pattern;

	public FileMatcher(Path directory, String... patterns) {
		this.directory = directory;
		this.pattern = compilePattern(patterns);
		if (this.pattern == null) {
			throw new IllegalArgumentException("Invalid pattern: " + pattern);
		}
	}

	@Override
	public boolean accept(File file) {
		return pattern.matcher(directory.relativize(file.toPath()).toString()).matches();
	}

	private static Pattern compilePattern(String... patterns) {
		List<String> patternsList = new ArrayList<>();
		for (String pattern : patterns) {
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
			for (String part : parts) {
				patternsList.add(part);
			}
		}

		StringBuilder pattern = new StringBuilder();
		Iterator<String> patternItr = patternsList.iterator();
		pattern.append("(").append(patternItr.next()).append(")");
		while (patternItr.hasNext()) {
			pattern.append("|(").append(patternItr.next()).append(")");
		}
		return Pattern.compile(pattern.toString());
	}

}
