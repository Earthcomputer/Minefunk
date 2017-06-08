package net.earthcomputer.minefunk.parser;

/**
 * The type definition of an integer
 * 
 * @author Earthcomputer
 */
public class IntDef extends ASTTypeDef {

	public static final IntDef INSTANCE = new IntDef();

	private IntDef() {
		super(MinefunkParserTreeConstants.JJTTYPEDEF);
		value = new ASTNodeValue(0, 0, 0, 0);
	}

}
