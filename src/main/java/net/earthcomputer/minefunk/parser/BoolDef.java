package net.earthcomputer.minefunk.parser;

/**
 * The type definition of a boolean
 * 
 * @author Earthcomputer
 */
public class BoolDef extends ASTTypeDef {

	public static final BoolDef INSTANCE = new BoolDef();

	private BoolDef() {
		super(MinefunkParserTreeConstants.JJTTYPEDEF);
		value = new ASTNodeValue(0, 0, 0, 0);
	}

}
