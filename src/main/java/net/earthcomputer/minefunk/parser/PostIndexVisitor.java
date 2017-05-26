package net.earthcomputer.minefunk.parser;

import net.earthcomputer.minefunk.Util;
import net.earthcomputer.minefunk.parser.Index.FunctionId;
import net.earthcomputer.minefunk.parser.IndexerVisitor.Data;

public class PostIndexVisitor extends IndexVisitor {

	@Override
	public Object visit(ASTCommandStmt node, Object data) {
		CommandParser.checkWildcardsAgainstIndex(node, ((Data) data).index, ((Data) data).exceptions);
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTFunctionCallExpr node, Object data) {
		// Visit children first to validate parameters
		super.visit(node, data);
		Node[] arguments = ASTUtil.getArguments(node);
		Type[] paramTypes = new Type[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			paramTypes[i] = ExpressionParser.getExpressionType(arguments[i], ((Data) data).index);
			if (paramTypes[i] == null) {
				// Possible if sub-function-call is invalid
				// Exit before we cause problems
				return data;
			}
			if (paramTypes[i].isVoid()) {
				((Data) data).exceptions
						.add(Util.createParseException("You cannot pass void to a function", arguments[i]));
				return data;
			}
		}
		if (((Data) data).index.getFrame()
				.resolveFunction(new FunctionId(ASTUtil.getFunctionName(node), paramTypes)) == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined function", node));
			return data;
		}
		return data;
	}

	@Override
	public Object visit(ASTFunction node, Object data) {
		if (((Data) data).index.getFrame().resolveType(ASTUtil.getReturnType(node)) == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined type", ASTUtil.getReturnTypeNode(node)));
		}
		super.visit(node, data);
		return data;
	}

	@Override
	public Object visit(ASTVarAccessExpr node, Object data) {
		if (((Data) data).index.getFrame().resolveVariableReference(ASTUtil.getVariable(node)) == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined variable", node));
		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		if (((Data) data).index.getFrame().resolveType(ASTUtil.getType(node)) == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined type", ASTUtil.getTypeNode(node)));
		}
		return super.visit(node, data);
	}

}
