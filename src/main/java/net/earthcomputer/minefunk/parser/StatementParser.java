package net.earthcomputer.minefunk.parser;

import java.util.List;

import static net.earthcomputer.minefunk.parser.MinefunkParserTreeConstants.*;

public class StatementParser {

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
