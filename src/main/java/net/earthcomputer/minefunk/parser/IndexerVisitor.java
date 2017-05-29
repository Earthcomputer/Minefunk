package net.earthcomputer.minefunk.parser;

import java.util.List;

/**
 * The AST visitor which adds members to the index
 * 
 * @author Earthcomputer
 */
public class IndexerVisitor extends IndexVisitor {

	@Override
	public Object visit(ASTTypeDef node, Object data) {
		((Data) data).index.addTypeDefinition(node, ((Data) data).exceptions);
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTFunction node, Object data) {
		((Data) data).index.addFunctionDefinition(node, ((Data) data).exceptions);
		super.visit(node, data);
		return data;
	}

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		if (!((Data) data).index.getFrame().isInBlock()) {
			((Data) data).index.addFieldDefinition(node, ((Data) data).exceptions);
		}
		return super.visit(node, data);
	}

	public static class Data implements IIndexVisitorData {
		public final Index index;
		public final List<ParseException> exceptions;

		public Data(Index index, List<ParseException> exceptions) {
			this.index = index;
			this.exceptions = exceptions;
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

}
