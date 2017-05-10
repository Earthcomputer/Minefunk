package net.earthcomputer.minefunk;

import java.io.StringReader;

import net.earthcomputer.minefunk.parser.ASTRoot;
import net.earthcomputer.minefunk.parser.MinefunkParser;
import net.earthcomputer.minefunk.parser.ParseException;

public class Main {

	public static void main(String[] args) throws ParseException {
		ASTRoot root = new MinefunkParser(new StringReader("exec foo")).parse();
		root.dump("");
	}

}
