package net.earthcomputer.minefunk.parser;

import java.util.Arrays;

public class ASTUtil {

	private ASTUtil() {
	};

	public static ASTNodeValue getNodeValue(Node node) {
		return (ASTNodeValue) ((SimpleNode) node).value;
	}

	public static Node[] getChildren(ASTBlockStmt stmt) {
		return stmt.children == null ? new Node[0] : stmt.children;
	}

	public static String getCommand(ASTCommandStmt stmt) {
		return (String) getNodeValue(stmt).getValue();
	}

	public static Node getExpression(ASTExpressionStmt stmt) {
		return stmt.children[0];
	}

	public static ASTModifiers getModifiersNode(ASTFunction function) {
		return (ASTModifiers) function.children[0];
	}

	public static int getModifiers(ASTFunction function) {
		return getModifiers(getModifiersNode(function));
	}

	public static ASTType getReturnTypeNode(ASTFunction function) {
		return (ASTType) function.children[1];
	}

	public static Type getReturnType(ASTFunction function) {
		return getType(getReturnTypeNode(function));
	}

	public static ASTIdentifier getNameNode(ASTFunction function) {
		return (ASTIdentifier) function.children[2];
	}

	public static String getName(ASTFunction function) {
		return getValue(getNameNode(function));
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
		return (Type) getNodeValue(expr).getValue();
	}

	public static Node[] getArguments(ASTFunctionCallExpr expr) {
		return expr.children == null ? new Node[0] : expr.children;
	}

	public static String getValue(ASTIdentifier id) {
		return (String) getNodeValue(id).getValue();
	}

	public static int getValue(ASTIntLiteralExpr expr) {
		return (int) getNodeValue(expr).getValue();
	}

	public static int getModifiers(ASTModifiers modifiers) {
		return (int) getNodeValue(modifiers).getValue();
	}

	public static String getName(ASTNamespace namespace) {
		return getValue((ASTIdentifier) namespace.children[0]);
	}

	public static Node[] getMembers(ASTNamespace namespace) {
		return Arrays.copyOfRange(namespace.children, 1, namespace.children.length);
	}

	public static ASTNamespace[] getNamespaces(ASTRoot root) {
		if (root.children == null) {
			return new ASTNamespace[0];
		}
		return Arrays.copyOf(root.children, root.children.length, ASTNamespace[].class);
	}

	public static String getValue(ASTStringLiteralExpr expr) {
		return (String) getNodeValue(expr).getValue();
	}

	public static Type getType(ASTType type) {
		return (Type) getNodeValue(type).getValue();
	}

	public static ASTIdentifier getNameNode(ASTTypeDef type) {
		return (ASTIdentifier) type.children[0];
	}

	public static String getName(ASTTypeDef type) {
		return getValue(getNameNode(type));
	}

	public static ASTType getVariableNode(ASTVarAccessExpr expr) {
		return (ASTType) expr.children[0];
	}

	public static Type getVariable(ASTVarAccessExpr expr) {
		return getType(getVariableNode(expr));
	}

	public static ASTModifiers getModifiersNode(ASTVarDeclStmt stmt) {
		return (ASTModifiers) stmt.children[0];
	}

	public static int getModifiers(ASTVarDeclStmt stmt) {
		return getModifiers(getModifiersNode(stmt));
	}

	public static ASTType getTypeNode(ASTVarDeclStmt stmt) {
		return (ASTType) stmt.children[1];
	}

	public static Type getType(ASTVarDeclStmt stmt) {
		return getType(getTypeNode(stmt));
	}

	public static ASTIdentifier getNameNode(ASTVarDeclStmt stmt) {
		return (ASTIdentifier) stmt.children[2];
	}

	public static String getName(ASTVarDeclStmt stmt) {
		return getValue(getNameNode(stmt));
	}

	public static Node getInitializer(ASTVarDeclStmt stmt) {
		if (stmt.children.length <= 3) {
			return null;
		} else {
			return stmt.children[3];
		}
	}

}
