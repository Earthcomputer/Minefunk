package net.earthcomputer.minefunk;

import java.util.ArrayList;
import java.util.List;

import net.earthcomputer.minefunk.parser.ASTRoot;
import net.earthcomputer.minefunk.parser.ASTValidator;
import net.earthcomputer.minefunk.parser.Index;
import net.earthcomputer.minefunk.parser.MinefunkParser;
import net.earthcomputer.minefunk.parser.ParseException;

public class Main {

	public static void main(String[] args) throws ParseException {
		ASTRoot root = new MinefunkParser(Main.class.getResourceAsStream("/stdlib/lang.funk")).parse();

		List<ParseException> exceptions = new ArrayList<>();

		ASTValidator.preIndexCheck(root, exceptions);
		if (handleExceptions(exceptions)) {
			return;
		}

		Index index = new Index();

		ASTValidator.index(root, index, exceptions);
		if (handleExceptions(exceptions)) {
			return;
		}

		index.resolvePendingFunctions(exceptions);
		if (handleExceptions(exceptions)) {
			return;
		}
	}
	
	private static boolean handleExceptions(List<ParseException> exceptions) {
		if (!exceptions.isEmpty()) {
			exceptions.forEach(ParseException::printStackTrace);
			return true;
		} else {
			return false;
		}
	}

}
