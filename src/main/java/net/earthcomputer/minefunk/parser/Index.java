package net.earthcomputer.minefunk.parser;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.earthcomputer.minefunk.Util;

public class Index {

	private Map<Type, ASTTypeDef> types = new HashMap<>();
	private Map<Type, ASTVarDeclStmt> fields = new HashMap<>();
	private Map<FunctionId, ASTFunction> functions = new HashMap<>();
	private Map<FunctionId, ASTFunction> functionsToResolve = new HashMap<>();
	private Deque<Frame> frames = new ArrayDeque<>();
	private Map<ASTVarDeclStmt, ExtFieldData> extFieldData = new IdentityHashMap<>();
	private Map<ASTFunction, ExtFunctionData> extFunctionData = new IdentityHashMap<>();
	private int nextFunctionId = 0;

	public Index() {
		addBuiltinTypes();
		// Root frame
		pushFrame(new ArrayDeque<>());
	}

	private void addBuiltinTypes() {
		types.put(Type.BOOL, new BoolDef());
		types.put(Type.INT, new IntDef());
		types.put(Type.STRING, new StringDef());
		types.put(Type.VOID, new VoidDef());
	}

	public void addTypeDefinition(ASTTypeDef typeDef, List<ParseException> exceptions) {
		Type type = new Type(frames.peek().getNamespacesList(), ASTUtil.getName(typeDef));
		if (types.containsKey(type)) {
			exceptions.add(new ParseException("Duplicate type declared: " + type));
		} else {
			types.put(type, typeDef);
		}
	}

	public void addFieldDefinition(ASTVarDeclStmt fieldDecl, List<ParseException> exceptions) {
		Type field = new Type(frames.peek().getNamespacesList(), ASTUtil.getName(fieldDecl));
		if (fields.containsKey(field)) {
			exceptions.add(new ParseException("Duplicate field declared: " + field));
		} else {
			fields.put(field, fieldDecl);
			extFieldData.put(fieldDecl, new ExtFieldData(field.getNamespaces()));
		}
	}

	public void addFunctionDefinition(ASTFunction func, List<ParseException> exceptions) {
		ASTVarDeclStmt[] rawParams = ASTUtil.getParameters(func);
		Type[] params = new Type[rawParams.length];
		for (int i = 0; i < rawParams.length; i++) {
			params[i] = ASTUtil.getType(rawParams[i]);
		}
		FunctionId funcId = new FunctionId(new Type(frames.peek().getNamespacesList(), ASTUtil.getName(func)), params);
		if (functionsToResolve.containsKey(funcId)) {
			exceptions.add(new ParseException("Duplicate function declared: " + funcId));
		} else {
			functionsToResolve.put(funcId, func);
		}
	}

	public void resolvePendingFunctions(List<ParseException> exceptions) {
		functionsToResolve.forEach((funcId, func) -> {
			pushFrame(Util.listToDeque(funcId.type.getNamespaces()));
			Type[] resolvedParams = new Type[funcId.paramTypes.length];
			boolean errored = false;
			for (int i = 0; i < resolvedParams.length; i++) {
				Type resolvedParam = getFrame().resolveType(funcId.paramTypes[i]);
				if (resolvedParam == null) {
					errored = true;
					exceptions.add(new ParseException("Could not resolve " + funcId.paramTypes[i]));
				} else {
					resolvedParams[i] = resolvedParam;
				}
			}
			if (!errored) {
				functions.put(new FunctionId(funcId.type, resolvedParams), func);
				extFunctionData.put(func, new ExtFunctionData(funcId.type.getNamespaces()));
			}
			popFrame();
		});
		functionsToResolve.clear();
	}

	public void pushFrame(Deque<String> namespaces) {
		frames.push(new Frame(this, namespaces, new ArrayDeque<>()));
	}

	public Frame getFrame() {
		return frames.peek();
	}

	public void popFrame() {
		frames.pop();
	}

	public ASTTypeDef getTypeDefintionNoContext(Type typeName) {
		return types.get(typeName);
	}

	public ASTVarDeclStmt getFieldDefinitionNoContext(Type fieldName) {
		return fields.get(fieldName);
	}

	public ASTFunction getFunctionDefinitionNoContext(FunctionId funcId) {
		return functions.get(funcId);
	}

	public ASTFunction getFunctionDefinitionNoContext(Type type, Type... paramTypes) {
		return functions.get(new FunctionId(type, paramTypes));
	}

	public boolean isField(ASTVarDeclStmt varDecl) {
		return fields.containsValue(varDecl);
	}

	public String getFunctionId(ASTFunction function) {
		ExtFunctionData extData = extFunctionData.get(function);
		if (extData.getId() == null) {
			StringBuilder newName = new StringBuilder();
			Iterator<String> nsItr = extData.getNamespaces().iterator();
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
				newName.append("0funk").append(nextFunctionId++);
			}
			extData.setId(newName.toString());
		}
		return extData.getId();
	}

	public ExtFieldData getExtFieldData(ASTVarDeclStmt field) {
		return extFieldData.get(field);
	}

	public ExtFunctionData getExtFunctionData(ASTFunction func) {
		return extFunctionData.get(func);
	}

	public static class FunctionId {
		private Type type;
		private Type[] paramTypes;

		public FunctionId(Type type, Type[] paramTypes) {
			this.type = type;
			this.paramTypes = paramTypes;
		}

		public Type getName() {
			return type;
		}

		public Type[] getParameters() {
			return paramTypes;
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
