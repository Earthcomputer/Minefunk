package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.earthcomputer.minefunk.Util;
import net.earthcomputer.minefunk.parser.Index.FunctionId;

public class Frame {

	private Index globalIndex;
	private Deque<String> namespaces;
	private Deque<Map<String, ASTVarDeclStmt>> localVariablesDefined;
	private Map<ASTVarDeclStmt, ExtLocalVariableData> extLocalVariableData = new IdentityHashMap<>();

	public Frame(Index globalIndex, Deque<String> namespaces,
			Deque<Map<String, ASTVarDeclStmt>> localVariablesDefined) {
		this.globalIndex = globalIndex;
		this.namespaces = namespaces;
		this.localVariablesDefined = localVariablesDefined;
	}

	public Index getGlobalIndex() {
		return globalIndex;
	}

	public List<String> getNamespacesList() {
		return Util.dequeToList(namespaces);
	}

	public Deque<String> getNamespaces() {
		return namespaces;
	}

	public Deque<Map<String, ASTVarDeclStmt>> getLocalVariablesDefined() {
		return localVariablesDefined;
	}

	public void pushNamespace(ASTNamespace namespace) {
		pushNamespace(ASTUtil.getName(namespace));
	}

	public void pushNamespace(String namespace) {
		namespaces.push(namespace);
	}

	public void popNamespace() {
		namespaces.pop();
	}

	public void pushBlock() {
		localVariablesDefined.push(new HashMap<>());
	}

	public void popBlock() {
		localVariablesDefined.pop().forEach((name, decl) -> extLocalVariableData.remove(decl));
	}

	public boolean isInBlock() {
		return !localVariablesDefined.isEmpty();
	}

	public void addLocalVariableDeclaration(ASTVarDeclStmt varDecl, List<ParseException> exceptions) {
		if (localVariablesDefined.peek().containsKey(ASTUtil.getName(varDecl))) {
			exceptions.add(new ParseException("Duplicate local variable defined"));
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
		extLocalVariableData.put(varDecl, new ExtLocalVariableData(constValue));
	}

	public void setConstLocalVariableValue(ASTVarDeclStmt varDecl, Object value) {
		extLocalVariableData.get(varDecl).setConstValue(value);
	}

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

	public Type resolveType(Type relativeType) {
		return resolve(relativeType, resolvedType -> globalIndex.getTypeDefintionNoContext(resolvedType) != null);
	}

	public Type resolveField(Type relativeField) {
		return resolve(relativeField, resolvedField -> globalIndex.getFieldDefinitionNoContext(resolvedField) != null);
	}

	public Type resolveFunction(FunctionId relativeFunction) {
		return resolve(relativeFunction.getName(), resolvedFunction -> globalIndex
				.getFunctionDefinitionNoContext(resolvedFunction, relativeFunction.getParameters()) != null);
	}

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
		return globalIndex.getFieldDefinitionNoContext(resolvedField);
	}

	public Object staticEvaluateVariable(Type varRef) {
		ASTVarDeclStmt varDecl = resolveVariableReference(varRef);
		if (varDecl == null) {
			return null;
		}
		if (!globalIndex.isField(varDecl)) {
			ExtLocalVariableData localVariableData = extLocalVariableData.get(varDecl);
			if (localVariableData == null) {
				return null;
			}
			return localVariableData.getConstValue();
		}
		if ((ASTUtil.getModifiers(varDecl) & Modifiers.CONST) == 0) {
			return null;
		}
		Node initializer = ASTUtil.getInitializer(varDecl);
		if (initializer == null) {
			return null;
		}
		globalIndex.pushFrame(Util.listToDeque(globalIndex.getExtFieldData(varDecl).getNamespaces()));
		try {
			return ExpressionParser.staticEvaluateExpression(initializer, globalIndex);
		} catch (ParseException e) {
			return null;
		} finally {
			globalIndex.popFrame();
		}
	}

}
