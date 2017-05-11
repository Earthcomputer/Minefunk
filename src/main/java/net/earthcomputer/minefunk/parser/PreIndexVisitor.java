package net.earthcomputer.minefunk.parser;

import java.util.List;

import static net.earthcomputer.minefunk.parser.ASTUtil.*;

public class PreIndexVisitor extends MinefunkParserDefaultVisitor implements MinefunkParserTreeConstants {

	@Override
	public Object visit(ASTCommandStmt node, Object data) {
		if (getCommand(node).endsWith(";")) {
			addException(data, new ParseException("Command statements should not end with a ; semicolon"));
		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTExpressionStmt node, Object data) {
		Node expr = getExpression(node);
		switch (expr.getId()) {
		case JJTFUNCTIONCALLEXPR:
			break;
		default:
			addException(data, new ParseException("You cannot use that type of expression as a statement"));
		}
		return super.visit(node, data);
	}

	private static final int ALLOWED_FUNCTION_MODIFIERS = Modifiers.INLINE;

	@Override
	public Object visit(ASTFunction node, Object data) {
		int modifiers = getModifiers(node);
		modifiers &= ~ALLOWED_FUNCTION_MODIFIERS;
		if (modifiers != 0) {
			addException(data, new ParseException("Invalid modifiers on function"));
		}
		return super.visit(node, data);
	}

	private static final int ALLOWED_VARIABLE_MODIFIERS = Modifiers.STATIC | Modifiers.CONST;

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		int modifiers = getModifiers(node);
		modifiers &= ~ALLOWED_VARIABLE_MODIFIERS;
		if (modifiers != 0) {
			addException(data, new ParseException("Invalid modifiers on function"));
		}
		return super.visit(node, data);
	}

	@SuppressWarnings("unchecked")
	private static void addException(Object data, ParseException e) {
		((List<ParseException>) data).add(e);
	}

}
