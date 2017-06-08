package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.earthcomputer.minefunk.Util;
import net.earthcomputer.minefunk.parser.Index.FunctionId;

/**
 * A context through which to resolve types, variables and functions. Allows for
 * omission of namespaces.
 * 
 * @author Earthcomputer
 */
public class Frame {

	private Index globalIndex;
	private Deque<String> namespaces;
	private Deque<Map<String, ASTVarDeclStmt>> localVariablesDefined;

	public Frame(Index globalIndex, Deque<String> namespaces,
			Deque<Map<String, ASTVarDeclStmt>> localVariablesDefined) {
		this.globalIndex = globalIndex;
		this.namespaces = namespaces;
		this.localVariablesDefined = localVariablesDefined;
	}

	/**
	 * Gets the global index containing the fully qualified names of all types,
	 * fields and functions
	 * 
	 * @return The global index
	 */
	public Index getGlobalIndex() {
		return globalIndex;
	}

	/**
	 * Gets the namespaces of the current context converted to a list.
	 * 
	 * @return The namespaces of the current context
	 * @see #getNamespaces()
	 */
	public List<String> getNamespacesList() {
		return Util.dequeToList(namespaces);
	}

	/**
	 * Gets the namespaces of the current context. Types, variables and
	 * functions are resolved relative to these namespaces
	 * 
	 * @return The namespaces of the current frame
	 */
	public Deque<String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Gets a collection of all local variables defined. What is actually
	 * returned is a deque of blocks, each of which can define local variables
	 * which may have the same name as a different block. Each block contains a
	 * map mapping the name of the variable to the variable declaration
	 * statement where it was declared.
	 * 
	 * @return The local variables defined
	 */
	public Deque<Map<String, ASTVarDeclStmt>> getLocalVariablesDefined() {
		return localVariablesDefined;
	}

	/**
	 * Pushes a new namespace onto the deque of namespaces.
	 * 
	 * @param namespace
	 *            - the namespace to push
	 * @see #pushNamespace(String)
	 */
	public void pushNamespace(ASTNamespace namespace) {
		pushNamespace(ASTUtil.getName(namespace));
	}

	/**
	 * Pushes a new namespace with the given name onto the deque of namespaces.
	 * Types, variables and functions will now be resolved relative to this
	 * namespace.
	 * 
	 * @param namespace
	 *            - the namespace to push
	 */
	public void pushNamespace(String namespace) {
		namespaces.push(namespace);
	}

	/**
	 * Pops a namespace from the deque of namespaces. Types, variables and
	 * functions will no longer be resolved relative to the top namespace.
	 */
	public void popNamespace() {
		namespaces.pop();
	}

	/**
	 * Pushes a block in which local variables can be declared
	 */
	public void pushBlock() {
		localVariablesDefined.push(new HashMap<>());
	}

	/**
	 * Pops a block in which local variables may have been declared
	 */
	public void popBlock() {
		localVariablesDefined.pop();
	}

	/**
	 * Gets whether we are inside a block and hence any variable declaration
	 * would be a local variable, as opposed to a field
	 * 
	 * @return Whether we are inside a block
	 */
	public boolean isInBlock() {
		return !localVariablesDefined.isEmpty();
	}

	/**
	 * Defines a local variable inside the current block
	 * 
	 * @param varDecl
	 *            - the local variable declaration
	 * @param exceptions
	 *            - the list of compiler errors to add to
	 */
	public void addLocalVariableDeclaration(ASTVarDeclStmt varDecl, List<ParseException> exceptions) {
		if (localVariablesDefined.peek().containsKey(ASTUtil.getName(varDecl))) {
			exceptions.add(Util.createParseException("Duplicate local variable defined", ASTUtil.getNameNode(varDecl)));
			return;
		}
		localVariablesDefined.peek().put(ASTUtil.getName(varDecl), varDecl);
		Object constValue = null;
		if ((ASTUtil.getModifiers(varDecl) & Modifiers.CONST) != 0) {
			Node initializer = ASTUtil.getInitializer(varDecl);
			if (initializer != null) {
				try {
					constValue = ExpressionParser.staticEvaluateExpression(initializer, globalIndex);
				} catch (ParseException e) {
					// Stays as null
				}
			}
		}
		ASTUtil.getNodeValue(varDecl).setUserData(Keys.CONST_VALUE, constValue);
	}

	/**
	 * Sets a local variable's constant value to some custom value
	 * 
	 * @param varDecl
	 *            - the variable whose constant value to change
	 * @param value
	 *            - the value to change to
	 */
	public void setConstLocalVariableValue(ASTVarDeclStmt varDecl, Object value) {
		ASTUtil.getNodeValue(varDecl).setUserData(Keys.CONST_VALUE, value);
	}

	/**
	 * Resolves a thing which has namespaces (e.g. a type, variable or
	 * function), relative to the current namespaces.
	 * 
	 * @param relativeType
	 *            - the thing to resolve
	 * @param existenceTest
	 *            - a test for whether a fully qualified thing exists
	 * @return The fully qualified thing resolved, or <tt>null</tt> if it
	 *         couldn't be resolved
	 */
	public Type resolve(Type relativeType, Predicate<Type> existenceTest) {
		List<String> relativeNamespaces = relativeType.getNamespaces();
		List<String> resolvedNamespaces = new ArrayList<>(namespaces.size() + relativeType.getNamespaces().size());
		Util.dequeToList(namespaces, resolvedNamespaces).addAll(relativeNamespaces);

		Type resolvedType = new Type(resolvedNamespaces, relativeType.getTypeName());

		while (resolvedNamespaces.size() >= relativeNamespaces.size()) {
			if (existenceTest.test(resolvedType)) {
				return resolvedType;
			}
			resolvedNamespaces.remove(0);
		}

		return null;
	}

	/**
	 * Resolves a type relative to the current namespaces
	 * 
	 * @param relativeType
	 *            - the type to resolve
	 * @return The resolved type, or <tt>null</tt> if it couldn't be resolved
	 */
	public Type resolveType(Type relativeType) {
		return resolve(relativeType, resolvedType -> globalIndex.getTypeDefinition(resolvedType) != null);
	}

	/**
	 * Resolves a field relative to the current namespaces
	 * 
	 * @param relativeField
	 *            - the field to resolve
	 * @return The resolved field, or <tt>null</tt> if it couldn't be resolved
	 */
	public Type resolveField(Type relativeField) {
		return resolve(relativeField, resolvedField -> globalIndex.getFieldDefinition(resolvedField) != null);
	}

	/**
	 * Resolves a function relative to the current namespaces
	 * 
	 * @param relativeFunction
	 *            - the function to resolve
	 * @return The name and namespaces of the resolved function, or
	 *         <tt>null</tt> if it couldn't be resolved
	 */
	public Type resolveFunction(FunctionId relativeFunction) {
		return resolve(relativeFunction.getName(), resolvedFunction -> globalIndex
				.getFunctionDefinition(resolvedFunction, relativeFunction.getParamTypes()) != null);
	}

	/**
	 * Resolves a variable reference to find its variable declaration. The
	 * variable reference may be resolved to a local variable or a field.
	 * 
	 * @param varRef
	 *            - the variable reference to resolve
	 * @return The declaration of the resolved variable, or <tt>null</tt> if it
	 *         couldn't be resolved
	 */
	public ASTVarDeclStmt resolveVariableReference(Type varRef) {
		if (varRef.getNamespaces().isEmpty()) {
			for (Map<String, ASTVarDeclStmt> block : localVariablesDefined) {
				if (block.containsKey(varRef.getTypeName())) {
					return block.get(varRef.getTypeName());
				}
			}
		}
		Type resolvedField = resolveField(varRef);
		if (resolvedField == null) {
			return null;
		}
		return globalIndex.getFieldDefinition(resolvedField);
	}

	/**
	 * Statically evaluates the given variable reference
	 * 
	 * @param varRef
	 *            - the variable reference to statically evaluate
	 * @return The result of the evaluation, or <tt>null</tt> if it couldn't be
	 *         statically evaluated
	 */
	public Object staticEvaluateVariable(Type varRef) {
		ASTVarDeclStmt varDecl = resolveVariableReference(varRef);
		if (varDecl == null) {
			return null;
		}
		if (!globalIndex.isField(varDecl)) {
			return ASTUtil.getNodeValue(varDecl).getUserData(Keys.CONST_VALUE);
		}
		if ((ASTUtil.getModifiers(varDecl) & Modifiers.CONST) == 0) {
			return null;
		}
		Node initializer = ASTUtil.getInitializer(varDecl);
		if (initializer == null) {
			return null;
		}
		globalIndex.pushFrame(Util.listToDeque(ASTUtil.getNodeValue(varDecl).getUserData(Keys.NAMESPACES)));
		try {
			return ExpressionParser.staticEvaluateExpression(initializer, globalIndex);
		} catch (ParseException e) {
			return null;
		} finally {
			globalIndex.popFrame();
		}
	}

}
