package net.earthcomputer.minefunk.parser;

import java.util.List;

public class ASTValidator {

	public static void preIndexCheck(ASTRoot root, List<ParseException> exceptions) {
		root.jjtAccept(new PreIndexVisitor(), exceptions);
	}

	public static void index(ASTRoot root, Index index, List<ParseException> exceptions) {
		root.jjtAccept(new IndexVisitor(), new IndexVisitor.Data(index, exceptions));
	}

}
