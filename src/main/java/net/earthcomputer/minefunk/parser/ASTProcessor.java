package net.earthcomputer.minefunk.parser;

import java.util.List;
import java.util.Map;

public class ASTProcessor {

	public static void preIndexCheck(ASTRoot root, List<ParseException> exceptions) {
		root.jjtAccept(new PreIndexVisitor(), exceptions);
	}

	public static void index(ASTRoot root, Index index, List<ParseException> exceptions) {
		root.jjtAccept(new IndexerVisitor(), new IndexerVisitor.Data(index, exceptions));
	}

	public static void postIndexCheck(ASTRoot root, Index index, List<ParseException> exceptions) {
		root.jjtAccept(new PostIndexVisitor(), new IndexerVisitor.Data(index, exceptions));
	}

	public static void generateCommandLists(ASTRoot root, Index index, Map<String, List<String>> commandLists,
			List<ParseException> exceptions) {
		root.jjtAccept(new CommandListVisitor(), new CommandListVisitor.Data(index, commandLists, exceptions));
	}

}
