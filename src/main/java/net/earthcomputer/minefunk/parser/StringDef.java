package net.earthcomputer.minefunk.parser;

/**
 * The type definition of a string
 * 
 * @author Earthcomputer
 */
public class StringDef extends ASTTypeDef {

	public static final StringDef INSTANCE = new StringDef();
	
	private StringDef() {
		super(MinefunkParserTreeConstants.JJTTYPEDEF);
		value = new ASTNodeValue(0, 0, 0, 0);
	}

}
