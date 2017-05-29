package net.earthcomputer.minefunk.parser;

import java.util.Arrays;

/**
 * A helper class to get fields from different types of AST nodes
 * 
 * @author Earthcomputer
 */
public class ASTUtil {

	private ASTUtil() {
	}

	/**
	 * Gets the <tt>ASTNodeValue</tt> of the given AST node
	 * 
	 * @param node
	 *            - the AST node
	 * @return The <tt>ASTNodeValue</tt>
	 */
	public static ASTNodeValue getNodeValue(Node node) {
		return (ASTNodeValue) ((SimpleNode) node).value;
	}

	/**
	 * Gets the child statements of a block statement
	 * 
	 * @param stmt
	 *            - the block statement
	 * @return The child statements
	 */
	public static Node[] getChildren(ASTBlockStmt stmt) {
		return stmt.children == null ? new Node[0] : stmt.children;
	}

	/**
	 * Gets the string contained in a command statement
	 * 
	 * @param stmt
	 *            - the command statement
	 * @return The command contained in the command statement (with wildcards
	 *         still present)
	 */
	public static String getCommand(ASTCommandStmt stmt) {
		return (String) getNodeValue(stmt).getValue();
	}

	/**
	 * Gets the expression contained in an expression statement
	 * 
	 * @param stmt
	 *            - the expression statement
	 * @return The contained expression
	 */
	public static Node getExpression(ASTExpressionStmt stmt) {
		return stmt.children[0];
	}

	/**
	 * Gets the <tt>ASTModifiers</tt> of a function
	 * 
	 * @param function
	 *            - the function
	 * @return The modifiers of the function
	 */
	public static ASTModifiers getModifiersNode(ASTFunction function) {
		return (ASTModifiers) function.children[0];
	}

	/**
	 * Gets the actual modifiers of a function
	 * 
	 * @param function
	 *            - the function
	 * @return The modifiers of the function
	 */
	public static int getModifiers(ASTFunction function) {
		return getModifiers(getModifiersNode(function));
	}

	/**
	 * Gets the return type of a function as an <tt>ASTType</tt>
	 * 
	 * @param function
	 *            - the function
	 * @return The return type of the function
	 */
	public static ASTType getReturnTypeNode(ASTFunction function) {
		return (ASTType) function.children[1];
	}

	/**
	 * Gets the actual return type of a function
	 * 
	 * @param function
	 *            - the function
	 * @return The return type of the function
	 */
	public static Type getReturnType(ASTFunction function) {
		return getType(getReturnTypeNode(function));
	}

	/**
	 * Gets the name of a function, as an <tt>ASTIdentifier</tt>
	 * 
	 * @param function
	 *            - the function
	 * @return The name of the function
	 */
	public static ASTIdentifier getNameNode(ASTFunction function) {
		return (ASTIdentifier) function.children[2];
	}

	/**
	 * Gets the actual name of a function
	 * 
	 * @param function
	 *            - the function
	 * @return The name of the function
	 */
	public static String getName(ASTFunction function) {
		return getValue(getNameNode(function));
	}

	/**
	 * Gets the parameters of a function
	 * 
	 * @param function
	 *            - the function
	 * @return The parameters of the function
	 */
	public static ASTVarDeclStmt[] getParameters(ASTFunction function) {
		Node[] nodes = ((ASTParamList) function.children[3]).children;
		if (nodes == null) {
			return new ASTVarDeclStmt[0];
		}
		ASTVarDeclStmt[] params = Arrays.copyOf(nodes, nodes.length, ASTVarDeclStmt[].class);
		return params;
	}

	/**
	 * Gets the body of a function
	 * 
	 * @param function
	 *            - the function
	 * @return The body of a function
	 */
	public static ASTBlockStmt getBody(ASTFunction function) {
		return (ASTBlockStmt) function.children[4];
	}

	/**
	 * Gets the function name in a function call expression
	 * 
	 * @param expr
	 *            - the function call expression
	 * @return The called function name
	 */
	public static Type getFunctionName(ASTFunctionCallExpr expr) {
		return (Type) getNodeValue(expr).getValue();
	}

	/**
	 * Gets the arguments in a function call expression
	 * 
	 * @param expr
	 *            - the function call expression
	 * @return An array of expressions containing the arguments the function is
	 *         called with
	 */
	public static Node[] getArguments(ASTFunctionCallExpr expr) {
		return expr.children == null ? new Node[0] : expr.children;
	}

	/**
	 * Gets the string value of an <tt>ASTIdentifier</tt>
	 * 
	 * @param id
	 *            - the identifier
	 * @return The string value
	 */
	public static String getValue(ASTIdentifier id) {
		return (String) getNodeValue(id).getValue();
	}

	/**
	 * Gets the integer value of an <tt>ASTIntLiteralExpr</tt>
	 * 
	 * @param expr
	 *            - the integer literal expression
	 * @return The integer value
	 */
	public static int getValue(ASTIntLiteralExpr expr) {
		return (int) getNodeValue(expr).getValue();
	}

	/**
	 * Gets the integer value of an <tt>ASTModifiers</tt> node
	 * 
	 * @param modifiers
	 *            - the modifiers
	 * @return The integer value
	 */
	public static int getModifiers(ASTModifiers modifiers) {
		return (int) getNodeValue(modifiers).getValue();
	}

	/**
	 * Gets the name of a namespace
	 * 
	 * @param namespace
	 *            - the namespace
	 * @return The name of the namespace
	 */
	public static String getName(ASTNamespace namespace) {
		return getValue((ASTIdentifier) namespace.children[0]);
	}

	/**
	 * Gets the members contained inside the namespace
	 * 
	 * @param namespace
	 *            - the namespace
	 * @return The members contained inside the namespace
	 */
	public static Node[] getMembers(ASTNamespace namespace) {
		return Arrays.copyOfRange(namespace.children, 1, namespace.children.length);
	}

	/**
	 * Gets all the namespaces contained in an AST tree
	 * 
	 * @param root
	 *            - the AST tree
	 * @return All the namespaces contained in the AST tree
	 */
	public static ASTNamespace[] getNamespaces(ASTRoot root) {
		if (root.children == null) {
			return new ASTNamespace[0];
		}
		return Arrays.copyOf(root.children, root.children.length, ASTNamespace[].class);
	}

	/**
	 * Gets the string value of an <tt>ASTStringLiteralExpr</tt>
	 * 
	 * @param expr
	 *            - the string literal expression
	 * @return The string value
	 */
	public static String getValue(ASTStringLiteralExpr expr) {
		return (String) getNodeValue(expr).getValue();
	}

	/**
	 * Gets the type represented by an <tt>ASTType</tt>
	 * 
	 * @param type
	 *            - the <tt>ASTType</tt>
	 * @return The represented type
	 */
	public static Type getType(ASTType type) {
		return (Type) getNodeValue(type).getValue();
	}

	/**
	 * Gets the <tt>ASTIdentifier</tt> representing the name of a type
	 * definition
	 * 
	 * @param type
	 *            - the type definition
	 * @return The name of the type defintion
	 */
	public static ASTIdentifier getNameNode(ASTTypeDef type) {
		return (ASTIdentifier) type.children[0];
	}

	/**
	 * Gets the name of a type definition
	 * 
	 * @param type
	 *            - the type definition
	 * @return The name of the type definition
	 */
	public static String getName(ASTTypeDef type) {
		return getValue(getNameNode(type));
	}

	/**
	 * Gets the <tt>ASTType</tt> representing the variable accessed by a
	 * variable access expression
	 * 
	 * @param expr
	 *            - the variable access expression
	 * @return The variable accessed
	 */
	public static ASTType getVariableNode(ASTVarAccessExpr expr) {
		return (ASTType) expr.children[0];
	}

	/**
	 * Gets the variable accessed by a variable access expression
	 * 
	 * @param expr
	 *            - the variable access expression
	 * @return The variable accessed
	 */
	public static Type getVariable(ASTVarAccessExpr expr) {
		return getType(getVariableNode(expr));
	}

	/**
	 * Gets the <tt>ASTModifiers</tt> of a variable declaration statement
	 * 
	 * @param stmt
	 *            - the variable declaration statement
	 * @return The modifiers of the variable declaration statement
	 */
	public static ASTModifiers getModifiersNode(ASTVarDeclStmt stmt) {
		return (ASTModifiers) stmt.children[0];
	}

	/**
	 * Gets the actual modifiers of the variable declaration statement
	 * 
	 * @param stmt
	 *            - the variable declaration statement
	 * @return The modifiers of the variable declaration statement
	 */
	public static int getModifiers(ASTVarDeclStmt stmt) {
		return getModifiers(getModifiersNode(stmt));
	}

	/**
	 * Gets the <tt>ASTType</tt> representing the type of the variable declared
	 * by a variable declaration statement
	 * 
	 * @param stmt
	 *            - the variable declaration statement
	 * @return The type of the variable declared
	 */
	public static ASTType getTypeNode(ASTVarDeclStmt stmt) {
		return (ASTType) stmt.children[1];
	}

	/**
	 * Gets the type of the variable declared by a variable declaration
	 * statement
	 * 
	 * @param stmt
	 *            - the variable declaration statement
	 * @return The type of the variable declared
	 */
	public static Type getType(ASTVarDeclStmt stmt) {
		return getType(getTypeNode(stmt));
	}

	/**
	 * Gets the <tt>ASTIdentifier</tt> representing the name of the variable
	 * declared by a variable declaration statement
	 * 
	 * @param stmt
	 *            - the variable declaration statement
	 * @return The type of the variable declared
	 */
	public static ASTIdentifier getNameNode(ASTVarDeclStmt stmt) {
		return (ASTIdentifier) stmt.children[2];
	}

	/**
	 * Gets the name of the variable declared by a variable declaration
	 * statement
	 * 
	 * @param stmt
	 *            - the variable declaration statement
	 * @return The type of the variable declared
	 */
	public static String getName(ASTVarDeclStmt stmt) {
		return getValue(getNameNode(stmt));
	}

	/**
	 * Gets the initialization expression used to initialize the variable
	 * declared by a variable declaration statement
	 * 
	 * @param stmt
	 *            - the variable declaration statement
	 * @return The expression used to initialize the variable
	 */
	public static Node getInitializer(ASTVarDeclStmt stmt) {
		if (stmt.children.length <= 3) {
			return null;
		} else {
			return stmt.children[3];
		}
	}

}
