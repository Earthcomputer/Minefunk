package net.earthcomputer.minefunk.parser;

import static net.earthcomputer.minefunk.parser.MinefunkParserTreeConstants.*;

public class ExpressionParser {

	private ExpressionParser() {
	}

	public static Type getExpressionType(Node node, Index index) {
		switch (node.getId()) {
		case JJTBOOLLITERALEXPR:
			return Type.BOOL;
		case JJTFUNCTIONCALLEXPR:
			ASTFunctionCallExpr funcCall = (ASTFunctionCallExpr) node;
			Node[] arguments = ASTUtil.getArguments(funcCall);
			Type[] paramTypes = new Type[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				paramTypes[i] = getExpressionType(arguments[i], index);
			}
			ASTFunction func = index.getFunctionDefinition(ASTUtil.getFunctionName(funcCall), paramTypes);
			if (func == null) {
				// Possible if not validated yet
				return null;
			}
			return index.resolveType(ASTUtil.getReturnType(func));
		case JJTINTLITERALEXPR:
			return Type.INT;
		case JJTSTRINGLITERALEXPR:
			return Type.STRING;
		case JJTVARACCESSEXPR:
			return index.resolveType(
					ASTUtil.getType(index.getVariableDeclaration(ASTUtil.getVariable((ASTVarAccessExpr) node))));
		default:
			throw new IllegalArgumentException("Unrecognized expression");
		}
	}

}
