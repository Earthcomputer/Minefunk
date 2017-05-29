package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.minefunk.Util;

/**
 * Utilities for processing the raw string inside a command statement
 * 
 * @author Earthcomputer
 */
public class CommandParser {

	private CommandParser() {
	}

	/**
	 * Gets a list of positions in the command where wildcards appear
	 * 
	 * @param commandStmt
	 *            - the command statement
	 * @return The list of positions of wildcards
	 * @throws ParseException
	 */
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

	/**
	 * Converts the wildcard at the given wildcard index to a type which may
	 * refer to a variable
	 * 
	 * @param commandStmt
	 *            - the command statement
	 * @param wildcardIndex
	 *            - the wildcard index
	 * @return The type representing a variable referred to
	 * @throws ParseException
	 */
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

	/**
	 * Checks the wildcards that occur in the given command statement against
	 * the index
	 * 
	 * @param commandStmt
	 *            - the command statement
	 * @param index
	 *            - the index
	 * @param exceptions
	 *            - the list of compiler errors to add to
	 */
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

	/**
	 * Converts the given command statement into a raw Minecraft command by
	 * static evaluating all the wildcards
	 * 
	 * @param commandStmt
	 *            - the command statement
	 * @param index
	 *            - the index
	 * @return The raw Minecraft command
	 * @throws ParseException
	 */
	public static String makeRawCommand(ASTCommandStmt commandStmt, Index index) throws ParseException {
		String command = ASTUtil.getCommand(commandStmt);
		StringBuilder newCommand = new StringBuilder(command);
		List<WildcardIndex> wildcardIndices = getWildcardIndexes(commandStmt);
		for (int i = wildcardIndices.size() - 1; i >= 0; i--) {
			WildcardIndex wildcardIndex = wildcardIndices.get(i);
			Type type = wildcardToType(commandStmt, wildcardIndex);
			Object value = index.getFrame().staticEvaluateVariable(type);
			if (value == null) {
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

	/**
	 * Create a compiler error with the wildcard index as the range in the
	 * source code
	 * 
	 * @param message
	 *            - the message
	 * @param commandStmt
	 *            - the command statement
	 * @param idx
	 *            - the wildcard index
	 * @return The parse exception
	 */
	private static ParseException createParseException(String message, ASTCommandStmt commandStmt, WildcardIndex idx) {
		ASTNodeValue value = ASTUtil.getNodeValue(commandStmt);
		return Util.createParseException(message,
				Util.createToken(ASTUtil.getCommand(commandStmt).substring(idx.startPercent + 1, idx.endPercent),
						value.getStartLine(), value.getStartColumn() + 1 + idx.startPercent, value.getEndLine(),
						value.getEndColumn() + 1 + idx.endPercent));
	}

	/**
	 * A wildcard index. Contains the index in the command string of the opening
	 * % and the closing % of the wildcard
	 * 
	 * @author Earthcomputer
	 */
	public static class WildcardIndex {
		public final int startPercent;
		public final int endPercent;

		public WildcardIndex(int startPercent, int endPercent) {
			this.startPercent = startPercent;
			this.endPercent = endPercent;
		}
	}

}
