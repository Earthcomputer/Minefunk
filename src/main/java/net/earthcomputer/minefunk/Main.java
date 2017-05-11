package net.earthcomputer.minefunk;

import net.earthcomputer.minefunk.parser.ASTRoot;
import net.earthcomputer.minefunk.parser.MinefunkParser;
import net.earthcomputer.minefunk.parser.ParseException;

public class Main {

	public static void main(String[] args) throws ParseException {
		ASTRoot root = new MinefunkParser(Main.class.getResourceAsStream("/stdlib/lang.funk")).parse();
		root.dump("");
	}

}
