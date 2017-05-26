package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.minefunk.Util;

public class CommandParser {

	private CommandParser() {
	}

	public static List<WildcardIndex> getWildcardIndexes(ASTCommandStmt commandStmt) throws ParseException {
		String command = ASTUtil.getCommand(commandStmt);
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
						throw Util.createParseException("Unclosed variable reference", commandStmt);
					}
					wildcardIndexes.add(new WildcardIndex(start, end));
				}
			}
		}
		return wildcardIndexes;
	}

	public static Type wildcardToType(ASTCommandStmt commandStmt, WildcardIndex wildcardIndex) throws ParseException {
		String command = ASTUtil.getCommand(commandStmt);
		String wildcard = command.substring(wildcardIndex.startPercent + 1, wildcardIndex.endPercent);
		wildcard = wildcard.trim();
		String[] parts = wildcard.split("\\s*::\\s*");
		if (parts.length == 0) {
			throw createParseException("Invalid variable reference", commandStmt, wildcardIndex);
		}
		for (String part : parts) {
			if (part.isEmpty()) {
				throw createParseException("Invalid variable reference", commandStmt, wildcardIndex);
			}
		}
		List<String> namespaces = new ArrayList<>();
		for (int i = 0; i < parts.length - 1; i++) {
			namespaces.add(parts[i]);
		}
		return new Type(namespaces, parts[parts.length - 1]);
	}

	public static void checkWildcardsAgainstIndex(ASTCommandStmt commandStmt, Index index,
			List<ParseException> exceptions) {
		List<WildcardIndex> wildcardIndexes;
		try {
			wildcardIndexes = getWildcardIndexes(commandStmt);
		} catch (ParseException e) {
			exceptions.add(e);
			return;
		}
		wildcardIndexes.forEach(wildcardIndex -> {
			Type type;
			try {
				type = wildcardToType(commandStmt, wildcardIndex);
			} catch (ParseException e) {
				exceptions.add(e);
				return;
			}
			if (index.getFrame().resolveVariableReference(type) == null) {
				exceptions.add(createParseException("Unrecognized variable", commandStmt, wildcardIndex));
			}
		});
	}

	public static String makeRawCommand(ASTCommandStmt commandStmt, Index index) throws ParseException {
		String command = ASTUtil.getCommand(commandStmt);
		StringBuilder newCommand = new StringBuilder(command);
		List<WildcardIndex> wildcardIndices = getWildcardIndexes(commandStmt);
		for (int i = wildcardIndices.size() - 1; i >= 0; i--) {
			WildcardIndex wildcardIndex = wildcardIndices.get(i);
			Type type = wildcardToType(commandStmt, wildcardIndex);
			Object value = index.getFrame().staticEvaluateVariable(type);
			if (value == null) {
				System.err.println("" + index.getFrame().getNamespacesList() + type);
				throw createParseException("Cannot static evaluate that variable", commandStmt, wildcardIndex);
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

	private static ParseException createParseException(String message, ASTCommandStmt commandStmt, WildcardIndex idx) {
		ASTNodeValue value = ASTUtil.getNodeValue(commandStmt);
		return Util.createParseException(message,
				Util.createToken(ASTUtil.getCommand(commandStmt).substring(idx.startPercent + 1, idx.endPercent),
						value.getStartLine(), value.getStartColumn() + 1 + idx.startPercent, value.getEndLine(),
						value.getEndColumn() + 1 + idx.endPercent));
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
