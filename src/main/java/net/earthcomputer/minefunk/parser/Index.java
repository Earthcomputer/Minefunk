package net.earthcomputer.minefunk.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Index {

	private Map<Type, ASTTypeDef> types = new HashMap<>();
	private Map<Type, ASTVarDeclStmt> fields = new HashMap<>();
	private Map<FunctionId, ASTFunction> functions = new HashMap<>();
	private Map<FunctionId, ASTFunction> functionsToResolve = new HashMap<>();
	private List<String> namespaces = new ArrayList<>();
	private Deque<Map<String, ASTVarDeclStmt>> localVariables = new ArrayDeque<>();

	public Index() {
		addBuiltinTypes();
	}

	private void addBuiltinTypes() {
		types.put(Type.BOOL, new BoolDef());
		types.put(Type.INT, new IntDef());
		types.put(Type.STRING, new StringDef());
		types.put(Type.VOID, new VoidDef());
	}

	public void addTypeDefinition(ASTTypeDef typeDef, List<ParseException> exceptions) {
		Type type = new Type(new ArrayList<>(namespaces), ASTUtil.getName(typeDef));
		if (types.containsKey(type)) {
			exceptions.add(new ParseException("Duplicate type declared: " + type));
		} else {
			types.put(type, typeDef);
		}
	}

	public void addFieldDefinition(ASTVarDeclStmt fieldDecl, List<ParseException> exceptions) {
		Type field = new Type(new ArrayList<>(namespaces), ASTUtil.getName(fieldDecl));
		if (fields.containsKey(field)) {
			exceptions.add(new ParseException("Duplicate field declared: " + field));
		} else {
			fields.put(field, fieldDecl);
		}
	}

	public void addFunctionDefinition(ASTFunction func, List<ParseException> exceptions) {
		ASTVarDeclStmt[] rawParams = ASTUtil.getParameters(func);
		Type[] params = new Type[rawParams.length];
		for (int i = 0; i < rawParams.length; i++) {
			params[i] = ASTUtil.getType(rawParams[i]);
		}
		FunctionId funcId = new FunctionId(new Type(new ArrayList<>(namespaces), ASTUtil.getName(func)), params);
		if (functionsToResolve.containsKey(funcId)) {
			exceptions.add(new ParseException("Duplicate function declared: " + funcId));
		} else {
			functionsToResolve.put(funcId, func);
		}
	}

	public void pushNamespace(ASTNamespace namespace) {
		namespaces.add(ASTUtil.getName(namespace));
	}

	public void popNamespace() {
		namespaces.remove(namespaces.size() - 1);
	}

	public void pushBlock() {
		localVariables.push(new HashMap<>());
	}

	public void popBlock() {
		localVariables.pop();
	}

	public boolean isInBlock() {
		return !localVariables.isEmpty();
	}

	public void addLocalVariableDeclaration(ASTVarDeclStmt varDecl, List<ParseException> exceptions) {
		String name = ASTUtil.getName(varDecl);
		if (localVariables.peek().containsKey(name)) {
			exceptions.add(new ParseException("Duplicate local variable: " + name));
		} else {
			localVariables.peek().put(ASTUtil.getName(varDecl), varDecl);
		}
	}

	public void resolvePendingFunctions(List<ParseException> exceptions) {
		functionsToResolve.forEach((funcId, func) -> {
			List<String> prevNamespaces = this.namespaces;
			this.namespaces = new ArrayList<>(funcId.type.getNamespaces());
			Type[] resolvedParams = new Type[funcId.paramTypes.length];
			boolean errored = false;
			for (int i = 0; i < resolvedParams.length; i++) {
				Type resolvedParam = resolveType(funcId.paramTypes[i]);
				if (resolvedParam == null) {
					errored = true;
					exceptions.add(new ParseException("Could not resolve " + funcId.paramTypes[i]));
				} else {
					resolvedParams[i] = resolvedParam;
				}
			}
			if (!errored) {
				functions.put(new FunctionId(funcId.type, resolvedParams), func);
			}
			this.namespaces = prevNamespaces;
		});
		functionsToResolve.clear();
	}

	private Type resolve(Type type, Map<Type, ?> map) {
		if (type.getNamespaces().isEmpty() && map.containsKey(type)) {
			return type;
		}
		List<String> namespaces = new ArrayList<>(this.namespaces);
		int idx = namespaces.size() - 1;
		namespaces.addAll(type.getNamespaces());
		Type tmpType = new Type(namespaces, type.getTypeName());
		while (idx >= 0) {
			if (map.containsKey(tmpType)) {
				return tmpType;
			}
			namespaces.remove(idx--);
		}
		return null;
	}

	public Type resolveType(Type type) {
		return resolve(type, types);
	}

	public Type resolveField(Type field) {
		return resolve(field, fields);
	}

	public FunctionId resolveFunction(FunctionId funcId) {
		Type[] params = new Type[funcId.paramTypes.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = resolveType(funcId.paramTypes[i]);
		}
		List<String> namespaces = new ArrayList<>(this.namespaces);
		int idx = namespaces.size() - 1;
		namespaces.addAll(funcId.type.getNamespaces());
		FunctionId tmpFuncId = new FunctionId(new Type(namespaces, funcId.type.getTypeName()), params);
		while (idx >= 0) {
			if (functions.containsKey(tmpFuncId)) {
				return tmpFuncId;
			}
			namespaces.remove(idx--);
		}
		if (funcId.type.getNamespaces().isEmpty()) {
			namespaces.clear();
			if (functions.containsKey(tmpFuncId)) {
				return tmpFuncId;
			}
		}
		return null;
	}

	public ASTTypeDef getTypeDefinition(Type typeName) {
		return types.get(resolveType(typeName));
	}

	public ASTTypeDef getTypeDefintionNoContext(Type typeName) {
		return types.get(typeName);
	}

	public ASTVarDeclStmt getVariableDeclaration(Type variableName) {
		if (variableName.getNamespaces().isEmpty()) {
			for (Map<String, ASTVarDeclStmt> frame : localVariables) {
				if (frame.containsKey(variableName.getTypeName())) {
					return frame.get(variableName.getTypeName());
				}
			}
		}
		return fields.get(resolveField(variableName));
	}

	public ASTVarDeclStmt getFieldDefinitionNoContext(Type fieldName) {
		return fields.get(fieldName);
	}

	public ASTFunction getFunctionDefinition(Type type, Type... paramTypes) {
		return functions.get(resolveFunction(new FunctionId(type, paramTypes)));
	}

	public ASTFunction getFunctionDefinitionNoContext(Type type, Type... paramTypes) {
		return functions.get(new FunctionId(type, paramTypes));
	}

	public static class FunctionId {
		private Type type;
		private Type[] paramTypes;

		public FunctionId(Type type, Type[] paramTypes) {
			this.type = type;
			this.paramTypes = paramTypes;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(paramTypes) + 31 * type.hashCode();
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
				return type.equals(funcId.type) && Arrays.equals(paramTypes, funcId.paramTypes);
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(type.toString());
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
