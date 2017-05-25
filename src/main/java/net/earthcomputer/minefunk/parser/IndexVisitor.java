package net.earthcomputer.minefunk.parser;

import java.util.List;

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

	public static interface IIndexVisitorData {
		Index getIndex();

		List<ParseException> getExceptions();
	}

}
