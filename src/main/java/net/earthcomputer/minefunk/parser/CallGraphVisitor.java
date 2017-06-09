package net.earthcomputer.minefunk.parser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This AST visitor adds from an AST tree to a call graph
 * 
 * @author Earthcomputer
 *
 */
public class CallGraphVisitor extends IndexVisitor {

	private Deque<CallGraphNode> currentNodeStack = new ArrayDeque<>();

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		currentNodeStack.push(
				new CallGraphNode(ASTUtil.getNodeValue(node).getUserData(Keys.ID), CallGraphNode.EnumType.VARIABLE));
		((Data) data).callGraph.put(currentNodeStack.peek(), new HashSet<>());
		super.visit(node, data);
		currentNodeStack.pop();
		return data;
	}

	@Override
	public Object visit(ASTVarAccessExpr node, Object data) {
		((Data) data).callGraph.get(currentNodeStack.peek()).add(
				new CallGraphNode(ASTUtil.getNodeValue(node).getUserData(Keys.ID), CallGraphNode.EnumType.VARIABLE));
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTCommandStmt node, Object data) {
		try {
			CommandParser.getWildcardIndexes(node).forEach(wildcardIndex -> {
				try {
					Type type = CommandParser.wildcardToType(node, wildcardIndex);
					ASTVarDeclStmt varRef = ((Data) data).index.getFrame().resolveVariableReference(type);
					((Data) data).callGraph.get(currentNodeStack.peek()).add(new CallGraphNode(
							ASTUtil.getNodeValue(varRef).getUserData(Keys.ID), CallGraphNode.EnumType.VARIABLE));
				} catch (ParseException e) {
					throw new Error(e);
				}
			});
		} catch (ParseException e) {
			throw new Error(e);
		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTFunction node, Object data) {
		currentNodeStack.push(
				new CallGraphNode(ASTUtil.getNodeValue(node).getUserData(Keys.ID), CallGraphNode.EnumType.FUNCTION));
		((Data) data).callGraph.put(currentNodeStack.peek(), new HashSet<>());
		super.visit(node, data);
		currentNodeStack.pop();
		return data;
	}

	@Override
	public Object visit(ASTFunctionCallExpr node, Object data) {
		((Data) data).callGraph.get(currentNodeStack.peek()).add(
				new CallGraphNode(ASTUtil.getNodeValue(node).getUserData(Keys.ID), CallGraphNode.EnumType.FUNCTION));
		return super.visit(node, data);
	}

	public static class Data implements IIndexVisitorData {
		public Index index;
		public List<ParseException> exceptions;
		public Map<CallGraphNode, Set<CallGraphNode>> callGraph;

		public Data(Index index, List<ParseException> exceptions, Map<CallGraphNode, Set<CallGraphNode>> callGraph) {
			this.index = index;
			this.exceptions = exceptions;
			this.callGraph = callGraph;
		}

		@Override
		public Index getIndex() {
			return index;
		}

		@Override
		public List<ParseException> getExceptions() {
			return exceptions;
		}
	}

	/**
	 * A call graph node. Contains the variable/function ID and the type (i.e.
	 * whether a node is a variable or a function)
	 * 
	 * @author Earthcomputer
	 */
	public static class CallGraphNode {
		private int id;
		private EnumType type;

		public CallGraphNode(int id, EnumType type) {
			this.id = id;
			this.type = type;
		}

		public int getId() {
			return id;
		}

		public EnumType getType() {
			return type;
		}

		@Override
		public int hashCode() {
			return id + 31 * type.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			} else if (!(other instanceof CallGraphNode)) {
				return false;
			} else {
				CallGraphNode otherNode = (CallGraphNode) other;
				return id == otherNode.id && type == otherNode.type;
			}
		}

		@Override
		public String toString() {
			return "(type=" + type + ",id=" + id + ")";
		}

		public static enum EnumType {
			VARIABLE, FUNCTION
		}
	}

}
