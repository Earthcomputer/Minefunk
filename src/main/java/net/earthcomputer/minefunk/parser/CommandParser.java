package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {

	private CommandParser() {
	}

	public static List<WildcardIndex> getWildcardIndexes(String command) throws ParseException {
		List<WildcardIndex> wildcardIndexes = new ArrayList<>();
		boolean escaped = false;
		for (int i = 0; i < command.length() - 1; i++) {
			if (command.charAt(i) == '%') {
				escaped = !escaped;
			} else {
				if (escaped) {
					escaped = false;
					int start = i - 1;
					int end = -1;
					for (; i < command.length(); i++) {
						if (command.charAt(i) == '%') {
							end = i;
							i++;
							break;
						}
					}
					if (end == -1) {
						throw new ParseException("Unlclosed variable reference");
					}
					wildcardIndexes.add(new WildcardIndex(start, end));
				}
			}
		}
		return wildcardIndexes;
	}

	public static Type wildcardToType(String command, WildcardIndex wildcardIndex) throws ParseException {
		String wildcard = command.substring(wildcardIndex.startPercent + 1, wildcardIndex.endPercent);
		wildcard = wildcard.trim();
		String[] parts = wildcard.split("\\s*::\\s*");
		if (parts.length == 0) {
			throw new ParseException("Invalid variable reference");
		}
		for (String part : parts) {
			if (part.isEmpty()) {
				throw new ParseException("Invalid variable reference");
			}
		}
		List<String> namespaces = new ArrayList<>();
		for (int i = 0; i < parts.length - 1; i++) {
			namespaces.add(parts[i]);
		}
		return new Type(namespaces, parts[parts.length - 1]);
	}

	public static void checkWildcardsAgainstIndex(String command, Index index, List<ParseException> exceptions) {
		List<WildcardIndex> wildcardIndexes;
		try {
			wildcardIndexes = getWildcardIndexes(command);
		} catch (ParseException e) {
			exceptions.add(e);
			return;
		}
		wildcardIndexes.forEach(wildcardIndex -> {
			Type type;
			try {
				type = wildcardToType(command, wildcardIndex);
			} catch (ParseException e) {
				exceptions.add(e);
				return;
			}
			if (index.getVariableDeclaration(type) == null) {
				exceptions.add(new ParseException("Unrecognized variable \"" + type + "\""));
			}
		});
	}

	public static class WildcardIndex {
		public final int startPercent;
		public final int endPercent;

		public WildcardIndex(int startPercent, int endPercent) {
			this.startPercent = startPercent;
			this.endPercent = endPercent;
		}
	}

}
