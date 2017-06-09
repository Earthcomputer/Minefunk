package net.earthcomputer.minefunk.parser;

import net.earthcomputer.minefunk.Util;
import net.earthcomputer.minefunk.parser.Index.FunctionId;
import net.earthcomputer.minefunk.parser.IndexerVisitor.Data;

/**
 * This is the AST visitor which performs post-index checks (things which we can
 * only know whether they are correct once we have finished the indexing phase),
 * such as type-checking
 * 
 * @author Earthcomputer
 */
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
		Type resolvedFunctionName = ((Data) data).index.getFrame()
				.resolveFunction(new FunctionId(ASTUtil.getFunctionName(node), paramTypes));
		if (resolvedFunctionName == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined function", node));
			return data;
		}
		ASTFunction function = ((Data) data).index.getFunctionDefinition(resolvedFunctionName, paramTypes);
		ASTUtil.getNodeValue(function).setUserData(Keys.REFERENCED, true);
		ASTUtil.getNodeValue(node).setUserData(Keys.ID, ASTUtil.getNodeValue(function).getUserData(Keys.ID));
		return data;
	}

	@Override
	public Object visit(ASTFunction node, Object data) {
		Type returnType = ((Data) data).index.getFrame().resolveType(ASTUtil.getReturnType(node));
		if (returnType == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined type", ASTUtil.getReturnTypeNode(node)));
		} else {
			ASTTypeDef returnTypeDef = ((Data) data).index.getTypeDefinition(returnType);
			ASTUtil.getNodeValue(node).setUserData(Keys.TYPE_ID,
					ASTUtil.getNodeValue(returnTypeDef).getUserData(Keys.ID));
		}
		super.visit(node, data);
		return data;
	}

	@Override
	public Object visit(ASTVarAccessExpr node, Object data) {
		ASTVarDeclStmt varDecl = ((Data) data).index.getFrame().resolveVariableReference(ASTUtil.getVariable(node));
		if (varDecl == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined variable", node));
		} else {
			ASTUtil.getNodeValue(node).setUserData(Keys.ID, ASTUtil.getNodeValue(varDecl).getUserData(Keys.ID));
			ASTUtil.getNodeValue(varDecl).setUserData(Keys.REFERENCED, true);
		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		Type varType = ((Data) data).index.getFrame().resolveType(ASTUtil.getType(node));
		if (varType == null) {
			((Data) data).exceptions.add(Util.createParseException("Undefined type", ASTUtil.getTypeNode(node)));
		} else {
			ASTTypeDef typeDef = ((Data) data).index.getTypeDefinition(varType);
			ASTUtil.getNodeValue(node).setUserData(Keys.TYPE_ID, ASTUtil.getNodeValue(typeDef).getUserData(Keys.ID));
		}
		return super.visit(node, data);
	}

}
