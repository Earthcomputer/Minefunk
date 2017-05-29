package net.earthcomputer.minefunk.parser;

import java.util.List;
import java.util.Map;

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
