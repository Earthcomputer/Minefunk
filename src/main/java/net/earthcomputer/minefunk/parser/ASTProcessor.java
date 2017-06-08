package net.earthcomputer.minefunk.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.earthcomputer.minefunk.CallGraphAnalyzer;

/**
 * Helper class for performing each stage of compilation on an AST tree
 * 
 * @author Earthcomputer
 */
public class ASTProcessor {

	/**
	 * Performs a pre-index check on an AST tree.
	 * 
	 * @param root
	 *            - the AST tree
	 * @param exceptions
	 *            - a list of compiler errors to add to
	 */
	public static void preIndexCheck(ASTRoot root, List<ParseException> exceptions) {
		root.jjtAccept(new PreIndexVisitor(), exceptions);
	}

	/**
	 * Indexes an AST tree.
	 * 
	 * @param root
	 *            - the AST tree
	 * @param index
	 *            - the index to add to
	 * @param exceptions
	 *            - a list of compiler errors to add to
	 */
	public static void index(ASTRoot root, Index index, List<ParseException> exceptions) {
		root.jjtAccept(new IndexerVisitor(), new IndexerVisitor.Data(index, exceptions));
	}

	/**
	 * Performs a post-index check on an AST tree
	 * 
	 * @param root
	 *            - the AST tree
	 * @param index
	 *            - the index
	 * @param exceptions
	 *            - a list of compiler errors to add to
	 */
	public static void postIndexCheck(ASTRoot root, Index index, List<ParseException> exceptions) {
		root.jjtAccept(new PostIndexVisitor(), new IndexerVisitor.Data(index, exceptions));
	}

	/**
	 * Adds to a call graph from an AST tree
	 * 
	 * @param callGraph
	 *            - the call graph
	 * @param root
	 *            - the AST tree
	 * @param index
	 *            - the index
	 * @param exceptions
	 *            - a list of compiler errors to add to
	 */
	public static void addToCallGraph(
			Map<CallGraphVisitor.CallGraphNode, Set<CallGraphVisitor.CallGraphNode>> callGraph, ASTRoot root,
			Index index, List<ParseException> exceptions) {
		CallGraphVisitor visitor = new CallGraphVisitor();
		root.jjtAccept(visitor, new CallGraphVisitor.Data(index, exceptions, callGraph));
	}

	/**
	 * Checks an AST tree for cyclic variable and inline function references,
	 * using a call graph having already been obtained using
	 * {@link #addToCallGraph(Map, ASTRoot, Index, List)}
	 * 
	 * @param cycleSearchResults
	 *            - the call graph cycle analysis results (see
	 *            {@link CallGraphAnalyzer}
	 * @param root
	 *            - the AST tree
	 * @param index
	 *            - the index
	 * @param exceptions
	 *            - a list of compiler errors to add to
	 */
	public static void checkForCyclicReferences(
			CallGraphAnalyzer.StronglyConnectedComponentsFinder.Result<CallGraphVisitor.CallGraphNode> cycleSearchResults,
			ASTRoot root, Index index, List<ParseException> exceptions) {
		root.jjtAccept(new CyclicReferencesFinderVisitor(),
				new CyclicReferencesFinderVisitor.Data(index, exceptions, cycleSearchResults));
	}

	/**
	 * Performs code generation for each function in the AST tree
	 * 
	 * @param root
	 *            - the AST tree
	 * @param index
	 *            - the index
	 * @param commandLists
	 *            - a map from the output function name to a list of Minecraft
	 *            commands that the output function will contain
	 * @param exceptions
	 *            - a list of compiler errors to add to
	 */
	public static void generateCommandLists(ASTRoot root, Index index, Map<String, List<String>> commandLists,
			List<ParseException> exceptions) {
		root.jjtAccept(new CommandListVisitor(), new CommandListVisitor.Data(index, commandLists, exceptions));
	}

}
