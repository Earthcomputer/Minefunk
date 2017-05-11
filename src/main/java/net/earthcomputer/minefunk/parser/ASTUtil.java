package net.earthcomputer.minefunk.parser;

import java.util.Arrays;

public class ASTUtil {

	private ASTUtil() {
	};

	public static String getCommand(ASTCommandStmt stmt) {
		return (String) stmt.value;
	}

	public static Node getExpression(ASTExpressionStmt stmt) {
		return stmt.children[0];
	}

	public static int getModifiers(ASTFunction function) {
		return getModifiers((ASTModifiers) function.children[0]);
	}

	public static Type getReturnType(ASTFunction function) {
		return getType((ASTType) function.children[1]);
	}

	public static String getName(ASTFunction function) {
		return getValue((ASTIdentifier) function.children[2]);
	}

	public static ASTVarDeclStmt[] getParameters(ASTFunction function) {
		Node[] nodes = ((ASTParamList) function.children[3]).children;
		if (nodes == null) {
			return new ASTVarDeclStmt[0];
		}
		ASTVarDeclStmt[] params = Arrays.copyOf(nodes, nodes.length, ASTVarDeclStmt[].class);
		return params;
	}

	public static ASTBlockStmt getBody(ASTFunction function) {
		return (ASTBlockStmt) function.children[4];
	}

	public static Type getFunctionName(ASTFunctionCallExpr expr) {
		return (Type) expr.value;
	}

	public static Node[] getArguments(ASTFunctionCallExpr expr) {
		return expr.children;
	}

	public static String getValue(ASTIdentifier id) {
		return (String) id.value;
	}

	public static int getValue(ASTIntLiteralExpr expr) {
		return (int) expr.value;
	}

	public static int getModifiers(ASTModifiers modifiers) {
		return (int) modifiers.value;
	}

	public static String getName(ASTNamespace namespace) {
		return getValue((ASTIdentifier) namespace.children[0]);
	}

	public static Node[] getMembers(ASTNamespace namespace) {
		return Arrays.copyOfRange(namespace.children, 1, namespace.children.length);
	}

	public static ASTNamespace[] getNamespaces(ASTRoot root) {
		return (ASTNamespace[]) root.children;
	}

	public static String getValue(ASTStringLiteralExpr expr) {
		return (String) expr.value;
	}

	public static Type getType(ASTType type) {
		return (Type) type.value;
	}

	public static String getName(ASTTypeDef type) {
		return getValue((ASTIdentifier) type.children[0]);
	}

	public static Type getVariable(ASTVarAccessExpr expr) {
		return getType((ASTType) expr.children[0]);
	}

	public static int getModifiers(ASTVarDeclStmt stmt) {
		return getModifiers((ASTModifiers) stmt.children[0]);
	}

	public static Type getType(ASTVarDeclStmt stmt) {
		return getType((ASTType) stmt.children[1]);
	}

	public static String getName(ASTVarDeclStmt stmt) {
		return getValue((ASTIdentifier) stmt.children[2]);
	}

}
