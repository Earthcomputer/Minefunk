package net.earthcomputer.minefunk.parser;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.earthcomputer.minefunk.Util;

/**
 * Contains the definitions of all types, fields and functions. This structure
 * is shared globally, so contains the fully qualified names of each
 * 
 * @author Earthcomputer
 */
public class Index {

	private Map<Type, ASTTypeDef> types = new HashMap<>();
	private Map<Type, ASTVarDeclStmt> fields = new HashMap<>();
	private Map<FunctionId, ASTFunction> functions = new HashMap<>();
	private Map<FunctionId, ASTFunction> functionsToResolve = new HashMap<>();
	private Deque<Frame> frames = new ArrayDeque<>();
	private Map<Integer, ASTTypeDef> typesById = new HashMap<>();
	private Map<Integer, ASTVarDeclStmt> variablesById = new HashMap<>();
	private Map<Integer, ASTFunction> functionsById = new HashMap<>();
	private int nextTypeId = 0;
	private int nextVariableId = 0;
	private int nextFunctionId = 0;

	public Index() {
		addBuiltinTypes();
		// Root frame
		pushFrame(new ArrayDeque<>());
	}

	private void addBuiltinTypes() {
		types.put(Type.BOOL, BoolDef.INSTANCE);
		types.put(Type.INT, IntDef.INSTANCE);
		types.put(Type.STRING, StringDef.INSTANCE);
		types.put(Type.VOID, VoidDef.INSTANCE);
		defineTypeId(BoolDef.INSTANCE);
		defineTypeId(IntDef.INSTANCE);
		defineTypeId(StringDef.INSTANCE);
		defineTypeId(VoidDef.INSTANCE);
	}

	/**
	 * Defines a type
	 * 
	 * @param typeDef
	 *            - the type to define
	 * @param exceptions
	 *            - the list of compiler errors to add to
	 */
	public void addTypeDefinition(ASTTypeDef typeDef, List<ParseException> exceptions) {
		Type type = new Type(frames.peek().getNamespacesList(), ASTUtil.getName(typeDef));
		if (types.containsKey(type)) {
			exceptions.add(Util.createParseException("Duplicate type declared: " + type, ASTUtil.getNameNode(typeDef)));
		} else {
			types.put(type, typeDef);
		}
	}

	/**
	 * Defines a field
	 * 
	 * @param fieldDecl
	 *            - the field to define
	 * @param exceptions
	 *            - the list of compiler errors to add to
	 */
	public void addFieldDefinition(ASTVarDeclStmt fieldDecl, List<ParseException> exceptions) {
		Type field = new Type(frames.peek().getNamespacesList(), ASTUtil.getName(fieldDecl));
		if (fields.containsKey(field)) {
			exceptions.add(
					Util.createParseException("Duplicate field declared: " + field, ASTUtil.getNameNode(fieldDecl)));
		} else {
			fields.put(field, fieldDecl);
			ASTUtil.getNodeValue(fieldDecl).setUserData(Keys.NAMESPACES, field.getNamespaces());
		}
	}

	/**
	 * Defines a function. Note that the function is not defined immediately
	 * because the parameter types may not have been defined yet. After
	 * indexing, {@link #resolvePendingFunctions(List)} must be called to ensure
	 * the functions are fully defined.
	 * 
	 * @param func
	 *            - the function to define
	 * @param exceptions
	 *            - the list of compiler errors to add to
	 */
	public void addFunctionDefinition(ASTFunction func, List<ParseException> exceptions) {
		ASTVarDeclStmt[] rawParams = ASTUtil.getParameters(func);
		Type[] params = new Type[rawParams.length];
		for (int i = 0; i < rawParams.length; i++) {
			params[i] = ASTUtil.getType(rawParams[i]);
		}
		FunctionId funcId = new FunctionId(new Type(frames.peek().getNamespacesList(), ASTUtil.getName(func)), params);
		if (functionsToResolve.containsKey(funcId)) {
			exceptions.add(
					Util.createParseException("Duplicate function declared: " + funcId, ASTUtil.getNameNode(func)));
		} else {
			functionsToResolve.put(funcId, func);
		}
	}

	/**
	 * Actually defines the functions after everything has been indexed
	 * 
	 * @param exceptions
	 *            - the list of compiler errors to add to
	 */
	public void resolvePendingFunctions(List<ParseException> exceptions) {
		functionsToResolve.forEach((funcId, func) -> {
			pushFrame(Util.listToDeque(funcId.name.getNamespaces()));
			Type[] resolvedParams = new Type[funcId.paramTypes.length];
			boolean errored = false;
			for (int i = 0; i < resolvedParams.length; i++) {
				Type resolvedParam = getFrame().resolveType(funcId.paramTypes[i]);
				if (resolvedParam == null) {
					errored = true;
					exceptions.add(Util.createParseException("Unknown type",
							ASTUtil.getTypeNode(ASTUtil.getParameters(func)[i])));
				} else {
					resolvedParams[i] = resolvedParam;
				}
			}
			if (!errored) {
				functions.put(new FunctionId(funcId.name, resolvedParams), func);
				ASTUtil.getNodeValue(func).setUserData(Keys.NAMESPACES, funcId.name.getNamespaces());
			}
			popFrame();
		});
		functionsToResolve.clear();
	}

	/**
	 * Pushes a new frame to the deque of frames
	 * 
	 * @param namespaces
	 *            - the namespaces of the new frame
	 */
	public void pushFrame(Deque<String> namespaces) {
		frames.push(new Frame(this, namespaces, new ArrayDeque<>()));
	}

	/**
	 * Gets the top frame on the deque of frames
	 * 
	 * @return The top frame
	 */
	public Frame getFrame() {
		return frames.peek();
	}

	/**
	 * Pops the top frame from the deque of frames
	 */
	public void popFrame() {
		frames.pop();
	}

	/**
	 * Gets a type definition, given a fully qualified type name
	 * 
	 * @param typeName
	 *            - the fully qualified type name
	 * @return The type definition, or <tt>null</tt> if the type was not defined
	 */
	public ASTTypeDef getTypeDefinition(Type typeName) {
		return types.get(typeName);
	}

	/**
	 * Gets a field definition, given a fully qualified field name
	 * 
	 * @param fieldName
	 *            - the fully qualified field name
	 * @return The field definition, or <tt>null</tt> if the field was not
	 *         defined
	 */
	public ASTVarDeclStmt getFieldDefinition(Type fieldName) {
		return fields.get(fieldName);
	}

	/**
	 * Gets a function definition, given a fully qualified function name and
	 * parameters
	 * 
	 * @param funcId
	 *            - the fully qualified function ID
	 * @return The function definition, or <tt>null</tt> if the function was not
	 *         resolved
	 */
	public ASTFunction getFunctionDefinition(FunctionId funcId) {
		return functions.get(funcId);
	}

	/**
	 * Gets a function definition, given a fully qualified function name and
	 * parameters
	 * 
	 * @param type
	 *            - the fully qualified name
	 * @param paramTypes
	 *            - the fully qualified names of the parameter types
	 * @return The function definition, or <tt>null</tt> if the function was not
	 *         resolved
	 */
	public ASTFunction getFunctionDefinition(Type type, Type... paramTypes) {
		return functions.get(new FunctionId(type, paramTypes));
	}

	/**
	 * Returns whether the given variable declaration declares a field
	 * 
	 * @param varDecl
	 *            - the variable declaration
	 * @return Whether the given variable is a field
	 */
	public boolean isField(ASTVarDeclStmt varDecl) {
		return fields.containsValue(varDecl);
	}

	/**
	 * Gets the final function name of the given function. This should be used
	 * instead of {@link ExtFunctionData#getId()}, as it creates a function ID
	 * if none was assigned yet.
	 * 
	 * @param function
	 *            - the function
	 * @return The final function name of the given function
	 */
	public String getFunctionId(ASTFunction function) {
		StringBuilder newName = new StringBuilder();
		Iterator<String> nsItr = ASTUtil.getNodeValue(function).getUserData(Keys.NAMESPACES).iterator();
		if (nsItr.hasNext()) {
			newName.append(nsItr.next()).append(":");
			while (nsItr.hasNext()) {
				newName.append(nsItr.next()).append("/");
			}
		}
		boolean noarg = ASTUtil.getParameters(function).length == 0;
		if (noarg) {
			newName.append(ASTUtil.getName(function));
		} else {
			newName.append("0funk").append(ASTUtil.getNodeValue(function).getUserData(Keys.ID));
		}
		return newName.toString();

	}

	public void defineTypeId(ASTTypeDef typeDef) {
		typesById.put(nextTypeId, typeDef);
		ASTUtil.getNodeValue(typeDef).setUserData(Keys.ID, nextTypeId++);
	}

	/**
	 * Gives a variable a unique ID
	 * 
	 * @param variable
	 *            - the variable to give an ID
	 */
	public void defineVariableId(ASTVarDeclStmt variable) {
		variablesById.put(nextVariableId, variable);
		ASTUtil.getNodeValue(variable).setUserData(Keys.ID, nextVariableId++);
	}

	/**
	 * Gives a function an unique ID
	 * 
	 * @param function
	 *            - the function to give an ID
	 */
	public void defineFunctionId(ASTFunction function) {
		functionsById.put(nextFunctionId, function);
		ASTUtil.getNodeValue(function).setUserData(Keys.ID, nextFunctionId++);
	}

	/**
	 * Returns the type assigned to the given ID
	 * 
	 * @param id
	 *            - the ID
	 * @return The type
	 */
	public ASTTypeDef getTypeById(int id) {
		return typesById.get(id);
	}

	/**
	 * Returns the variable declaration assigned to the given variable ID
	 * 
	 * @param id
	 *            - the ID
	 * @return The variable declaration statement
	 */
	public ASTVarDeclStmt getVariableById(int id) {
		return variablesById.get(id);
	}

	/**
	 * Returns the function declaration assigned to the given function ID
	 * 
	 * @param id
	 *            - the ID
	 * @return The function declaration
	 */
	public ASTFunction getFunctionById(int id) {
		return functionsById.get(id);
	}

	/**
	 * A class which stores the name and parameter types of a function, both of
	 * which are used to identify functions.
	 * 
	 * @author Earthcomputer
	 */
	public static class FunctionId {
		private Type name;
		private Type[] paramTypes;

		public FunctionId(Type name, Type[] paramTypes) {
			this.name = name;
			this.paramTypes = paramTypes;
		}

		/**
		 * Gets the function name
		 * 
		 * @return The function name
		 */
		public Type getName() {
			return name;
		}

		/**
		 * Gets the type names of the parameters
		 * 
		 * @return The parameter types
		 */
		public Type[] getParamTypes() {
			return paramTypes;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(paramTypes) + 31 * name.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			} else if (other == null) {
				return false;
			} else if (other.getClass() != FunctionId.class) {
				return false;
			} else {
				FunctionId funcId = (FunctionId) other;
				return name.equals(funcId.name) && Arrays.equals(paramTypes, funcId.paramTypes);
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(name.toString());
			sb.append('(');
			boolean first = true;
			for (Type paramType : paramTypes) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(paramType);
				first = false;
			}
			sb.append(')');
			return sb.toString();
		}
	}

}
