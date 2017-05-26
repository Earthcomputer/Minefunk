package net.earthcomputer.minefunk.parser;

import static net.earthcomputer.minefunk.parser.ASTUtil.getCommand;
import static net.earthcomputer.minefunk.parser.ASTUtil.getExpression;
import static net.earthcomputer.minefunk.parser.ASTUtil.getModifiers;
import static net.earthcomputer.minefunk.parser.ASTUtil.getParameters;
import static net.earthcomputer.minefunk.parser.ASTUtil.getReturnType;

import java.util.List;

import net.earthcomputer.minefunk.Util;

public class PreIndexVisitor extends MinefunkParserDefaultVisitor implements MinefunkParserTreeConstants {

	@Override
	public Object visit(ASTCommandStmt node, Object data) {
		if (getCommand(node).endsWith(";")) {
			addException(data, Util.createParseException("Command statements should not end with a ; semicolon", node));
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
			addException(data,
					Util.createParseException("You cannot use that type of expression as a statement", node));
		}
		return super.visit(node, data);
	}

	private static final int ALLOWED_FUNCTION_MODIFIERS = Modifiers.INLINE;

	@Override
	public Object visit(ASTFunction node, Object data) {
		int modifiers = getModifiers(node);
		if ((modifiers & Modifiers.INLINE) == 0) {
			if (getParameters(node).length != 0) {
				addException(data,
						Util.createParseException("Non-inline functions with parameters are not supported yet", node));
			}
		}
		if (!getReturnType(node).isVoid()) {
			addException(data, Util.createParseException("Non-void functions are not supported yet",
					ASTUtil.getReturnTypeNode(node)));
		}
		modifiers &= ~ALLOWED_FUNCTION_MODIFIERS;
		if (modifiers != 0) {
			addException(data,
					Util.createParseException("Invalid modifiers on function", ASTUtil.getModifiersNode(node)));
		}
		return super.visit(node, data);
	}

	private static final int ALLOWED_VARIABLE_MODIFIERS = Modifiers.CONST;

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		int modifiers = getModifiers(node);
		if ((modifiers & Modifiers.CONST) == 0) {
			addException(data, Util.createParseException("Non-const variables not supported yet", node));
		}
		modifiers &= ~ALLOWED_VARIABLE_MODIFIERS;
		if (modifiers != 0) {
			addException(data,
					Util.createParseException("Invalid modifiers on function", ASTUtil.getModifiersNode(node)));
		}
		return super.visit(node, data);
	}

	@SuppressWarnings("unchecked")
	private static void addException(Object data, ParseException e) {
		((List<ParseException>) data).add(e);
	}

}
