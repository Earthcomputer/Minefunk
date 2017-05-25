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
			if (index.getFrame().resolveVariableReference(type) == null) {
				exceptions.add(new ParseException("Unrecognized variable \"" + type + "\""));
			}
		});
	}

	public static String makeRawCommand(String command, Index index) throws ParseException {
		StringBuilder newCommand = new StringBuilder(command);
		List<WildcardIndex> wildcardIndices = getWildcardIndexes(command);
		for (int i = wildcardIndices.size() - 1; i >= 0; i--) {
			WildcardIndex wildcardIndex = wildcardIndices.get(i);
			Type type = wildcardToType(command, wildcardIndex);
			Object value = index.getFrame().staticEvaluateVariable(type);
			if (value == null) {
				System.err.println("" + index.getFrame().getNamespacesList() + type);
				throw new ParseException("Cannot static evaluate that variable");
			}
			newCommand.replace(wildcardIndex.startPercent, wildcardIndex.endPercent + 1, value.toString());
		}
		for (int i = 0; i < newCommand.length() - 1; i++) {
			if (newCommand.charAt(i) == '%' && newCommand.charAt(i + 1) == '%') {
				newCommand.deleteCharAt(i);
			}
		}
		return newCommand.toString();
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
