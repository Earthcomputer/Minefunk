package net.earthcomputer.minefunk.parser;

import java.util.List;

public class IndexVisitor extends MinefunkParserDefaultVisitor {

	@Override
	public Object visit(ASTNamespace node, Object data) {
		((Data) data).index.pushNamespace(node);
		super.visit(node, data);
		((Data) data).index.popNamespace();
		return data;
	}

	@Override
	public Object visit(ASTBlockStmt node, Object data) {
		((Data) data).index.pushBlock();
		super.visit(node, data);
		((Data) data).index.popBlock();
		return data;
	}

	@Override
	public Object visit(ASTTypeDef node, Object data) {
		((Data) data).index.addTypeDefinition(node, ((Data) data).exceptions);
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTFunction node, Object data) {
		((Data) data).index.addFunctionDefinition(node, ((Data) data).exceptions);
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTVarDeclStmt node, Object data) {
		if (!((Data) data).index.isInBlock()) {
			((Data) data).index.addFieldDefinition(node, ((Data) data).exceptions);
		}
		return super.visit(node, data);
	}

	public static class Data {
		public final Index index;
		public final List<ParseException> exceptions;

		public Data(Index index, List<ParseException> exceptions) {
			this.index = index;
			this.exceptions = exceptions;
		}
	}

}
