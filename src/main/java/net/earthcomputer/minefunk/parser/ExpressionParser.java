package net.earthcomputer.minefunk.parser;

import static net.earthcomputer.minefunk.parser.MinefunkParserTreeConstants.*;

import java.util.List;

import net.earthcomputer.minefunk.Util;
import net.earthcomputer.minefunk.parser.Index.FunctionId;

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
			ASTFunction func = index.getFunctionDefinitionNoContext(
					index.getFrame().resolveFunction(new FunctionId(ASTUtil.getFunctionName(funcCall), paramTypes)),
					paramTypes);
			if (func == null) {
				// Possible if not validated yet
				return null;
			}
			index.pushFrame(Util.listToDeque(index.getExtFunctionData(func).getNamespaces()));
			Type resolvedType = index.getFrame().resolveType(ASTUtil.getReturnType(func));
			index.popFrame();
			return resolvedType;
		case JJTINTLITERALEXPR:
			return Type.INT;
		case JJTSTRINGLITERALEXPR:
			return Type.STRING;
		case JJTVARACCESSEXPR:
			return index.getFrame().resolveType(ASTUtil
					.getType(index.getFrame().resolveVariableReference(ASTUtil.getVariable((ASTVarAccessExpr) node))));
		default:
			throw new IllegalArgumentException("Unrecognized expression");
		}
	}

	public static Object staticEvaluateExpression(Node node, Index index) throws ParseException {
		switch (node.getId()) {
		case JJTBOOLLITERALEXPR:
			return ASTUtil.getNodeValue(node).getValue();
		case JJTFUNCTIONCALLEXPR:
			throw cantStaticEvaluate(node);
		case JJTINTLITERALEXPR:
			return ASTUtil.getNodeValue(node).getValue();
		case JJTSTRINGLITERALEXPR:
			return ASTUtil.getNodeValue(node).getValue();
		case JJTVARACCESSEXPR:
			Object constValue = index.getFrame().staticEvaluateVariable(ASTUtil.getVariable((ASTVarAccessExpr) node));
			if (constValue == null) {
				throw cantStaticEvaluate(node);
			} else {
				return constValue;
			}
		default:
			throw new IllegalArgumentException("Unrecognized expression");
		}
	}

	public static void toCommandList(Node expr, Index index, List<String> commands, List<ParseException> exceptions) {
		switch (expr.getId()) {
		case JJTBOOLLITERALEXPR:
			return;
		case JJTFUNCTIONCALLEXPR:
			ASTFunctionCallExpr callExpr = (ASTFunctionCallExpr) expr;
			Node[] arguments = ASTUtil.getArguments(callExpr);
			Type[] paramTypes = new Type[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				paramTypes[i] = getExpressionType(arguments[i], index);
			}
			ASTFunction func = index.getFunctionDefinitionNoContext(
					index.getFrame().resolveFunction(new FunctionId(ASTUtil.getFunctionName(callExpr), paramTypes)),
					paramTypes);
			int modifiers = ASTUtil.getModifiers(func);
			if ((modifiers & Modifiers.INLINE) != 0) {
				ASTVarDeclStmt[] parameters = ASTUtil.getParameters(func);
				Object[] constValues = new Object[parameters.length];
				for (int i = 0; i < arguments.length; i++) {
					try {
						constValues[i] = ExpressionParser.staticEvaluateExpression(arguments[i], index);
					} catch (ParseException e) {
						continue;
					}
				}
				index.pushFrame(Util.listToDeque(index.getExtFunctionData(func).getNamespaces()));
				index.getFrame().pushBlock();
				for (int i = 0; i < parameters.length; i++) {
					index.getFrame().addLocalVariableDeclaration(parameters[i], exceptions);
					if (constValues[i] != null) {
						index.getFrame().setConstLocalVariableValue(parameters[i], constValues[i]);
					}
				}
				StatementParser.toCommandList(ASTUtil.getBody(func), index, commands, exceptions);
				index.popFrame();
			} else {
				commands.add("function " + index.getFunctionId(func));
			}
		case JJTINTLITERALEXPR:
			return;
		case JJTSTRINGLITERALEXPR:
			return;
		case JJTVARACCESSEXPR:
			return;
		default:
			throw new IllegalArgumentException("Unknown expression type");
		}
	}

	public static ParseException cantStaticEvaluate(Node expr) {
		return Util.createParseException("Can't static evaluate that expression", expr);
	}

}
