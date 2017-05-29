package net.earthcomputer.minefunk.parser;

import java.util.List;

import static net.earthcomputer.minefunk.parser.MinefunkParserTreeConstants.*;

/**
 * Utility class for performing operations on statements
 * 
 * @author Earthcomputer
 */
public class StatementParser {

	/**
	 * Compiles the given statement to a raw Minecraft command list
	 * 
	 * @param stmt
	 *            - the statement
	 * @param index
	 *            - the index
	 * @param commands
	 *            - the list of commands to add to
	 * @param exceptions
	 *            - the list of compiler errors to add to
	 */
	public static void toCommandList(Node stmt, Index index, List<String> commands, List<ParseException> exceptions) {
		switch (stmt.getId()) {
		case JJTBLOCKSTMT:
			index.getFrame().pushBlock();
			for (Node child : ASTUtil.getChildren((ASTBlockStmt) stmt)) {
				toCommandList(child, index, commands, exceptions);
			}
			index.getFrame().popBlock();
			break;
		case JJTCOMMANDSTMT:
			String rawCommand;
			try {
				rawCommand = CommandParser.makeRawCommand((ASTCommandStmt) stmt, index);
			} catch (ParseException e) {
				exceptions.add(e);
				return;
			}
			commands.add(rawCommand);
			break;
		case JJTEXPRESSIONSTMT:
			ExpressionParser.toCommandList(ASTUtil.getExpression((ASTExpressionStmt) stmt), index, commands,
					exceptions);
			break;
		case JJTVARDECLSTMT:
			index.getFrame().addLocalVariableDeclaration((ASTVarDeclStmt) stmt, exceptions);
			break;
		}
	}

}
