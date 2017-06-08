package net.earthcomputer.minefunk.parser;

import java.util.List;

import net.earthcomputer.minefunk.CallGraphAnalyzer;
import net.earthcomputer.minefunk.Util;

/**
 * Finds cycles in a call graph. Uses an AST tree so we know what files the
 * errors occur in.
 * 
 * @author Earthcomputer
 */
public class CyclicReferencesFinderVisitor extends MinefunkParserDefaultVisitor {

	@Override
	public Object visit(ASTFunction node, Object data) {
		if ((ASTUtil.getModifiers(node) & Modifiers.INLINE) != 0) {
			if (((Data) data).cycleSearchResults.isConnectedComponent(
					new CallGraphVisitor.CallGraphNode(ASTUtil.getNodeValue(node).getUserData(Keys.ID),
							CallGraphVisitor.CallGraphNode.EnumType.FUNCTION))) {
				((Data) data).exceptions.add(cycRefFound(node));
			}
		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		if (((Data) data).cycleSearchResults.isConnectedComponent(new CallGraphVisitor.CallGraphNode(
				ASTUtil.getNodeValue(node).getUserData(Keys.ID), CallGraphVisitor.CallGraphNode.EnumType.VARIABLE))) {
			((Data) data).exceptions.add(cycRefFound(node));
		}
		return super.visit(node, data);
	}

	private static ParseException cycRefFound(Node node) {
		return Util.createParseException("Cyclic variable/inline function referneces detected", node);
	}

	public static class Data {
		public Index index;
		public List<ParseException> exceptions;
		public CallGraphAnalyzer.StronglyConnectedComponentsFinder.Result<CallGraphVisitor.CallGraphNode> cycleSearchResults;

		public Data(Index index, List<ParseException> exceptions,
				CallGraphAnalyzer.StronglyConnectedComponentsFinder.Result<CallGraphVisitor.CallGraphNode> cycleSearchResults) {
			this.index = index;
			this.exceptions = exceptions;
			this.cycleSearchResults = cycleSearchResults;
		}
	}

}
