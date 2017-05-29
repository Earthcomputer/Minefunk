package net.earthcomputer.minefunk.parser;

import java.util.List;

/**
 * Any visitor which walks through an AST tree with the purpose of performing
 * operations with the index. This class overrides appropriate methods to
 * automatically push and pop namespaces etc.
 * 
 * @author Earthcomputer
 */
public abstract class IndexVisitor extends MinefunkParserDefaultVisitor {

	@Override
	public Object visit(ASTNamespace node, Object data) {
		getIndex(data).getFrame().pushNamespace(node);
		super.visit(node, data);
		getIndex(data).getFrame().popNamespace();
		return data;
	}

	@Override
	public Object visit(ASTFunction node, Object data) {
		getIndex(data).getFrame().pushBlock();
		super.visit(node, data);
		getIndex(data).getFrame().popBlock();
		return data;
	}

	@Override
	public Object visit(ASTBlockStmt node, Object data) {
		getIndex(data).getFrame().pushBlock();
		super.visit(node, data);
		getIndex(data).getFrame().popBlock();
		return data;
	}

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		if (getIndex(data).getFrame().isInBlock()) {
			getIndex(data).getFrame().addLocalVariableDeclaration(node, getExceptions(data));
		}
		return super.visit(node, data);
	}

	private static Index getIndex(Object data) {
		return ((IIndexVisitorData) data).getIndex();
	}

	private static List<ParseException> getExceptions(Object data) {
		return ((IIndexVisitorData) data).getExceptions();
	}

	/**
	 * All subclasses of <tt>IndexVisitor</tt> must work on data which are
	 * instances of this interface
	 * 
	 * @author Earthcomputer
	 */
	public static interface IIndexVisitorData {
		/**
		 * Gets the index we are working with
		 * 
		 * @return The index we are working with
		 */
		Index getIndex();

		/**
		 * Gets the list of compiler errors to add to
		 * 
		 * @return The list of compiler errors to add to
		 */
		List<ParseException> getExceptions();
	}

}
