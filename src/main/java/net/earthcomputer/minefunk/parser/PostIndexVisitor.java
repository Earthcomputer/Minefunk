package net.earthcomputer.minefunk.parser;

import net.earthcomputer.minefunk.parser.Index.FunctionId;
import net.earthcomputer.minefunk.parser.IndexVisitor.Data;

public class PostIndexVisitor extends MinefunkParserDefaultVisitor {

	@Override
	public Object visit(ASTBlockStmt node, Object data) {
		((Data) data).index.pushBlock();
		super.visit(node, data);
		((Data) data).index.popBlock();
		return data;
	}

	@Override
	public Object visit(ASTCommandStmt node, Object data) {
		CommandParser.checkWildcardsAgainstIndex(ASTUtil.getCommand(node), ((Data) data).index,
				((Data) data).exceptions);
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
				((Data) data).exceptions.add(new ParseException("You cannot pass void to a function"));
				return data;
			}
		}
		if (((Data) data).index.resolveFunction(new FunctionId(ASTUtil.getFunctionName(node), paramTypes)) == null) {
			((Data) data).exceptions.add(new ParseException("Undefined function"));
			return data;
		}
		return data;
	}

	@Override
	public Object visit(ASTFunction node, Object data) {
		if (((Data) data).index.resolveType(ASTUtil.getReturnType(node)) == null) {
			((Data) data).exceptions.add(new ParseException("Undefined type"));
		}
		// Push a block so we can declare parameters
		((Data) data).index.pushBlock();
		super.visit(node, data);
		((Data) data).index.popBlock();
		return data;
	}

	@Override
	public Object visit(ASTNamespace node, Object data) {
		((Data) data).index.pushNamespace(node);
		super.visit(node, data);
		((Data) data).index.popNamespace();
		return data;
	}

	@Override
	public Object visit(ASTVarAccessExpr node, Object data) {
		if (((Data) data).index.getVariableDeclaration(ASTUtil.getVariable(node)) == null) {
			((Data) data).exceptions.add(new ParseException("Undefined variable"));
		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		if (((Data) data).index.resolveType(ASTUtil.getType(node)) == null) {
			((Data) data).exceptions.add(new ParseException("Undefined type"));
		}
		if (((Data) data).index.isInBlock()) {
			((Data) data).index.addLocalVariableDeclaration(node, ((Data) data).exceptions);
		}
		return super.visit(node, data);
	}

}
