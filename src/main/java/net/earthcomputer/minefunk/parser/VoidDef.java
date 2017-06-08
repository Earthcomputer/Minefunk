package net.earthcomputer.minefunk.parser;

/**
 * The type definition of void
 * 
 * @author Earthcomputer
 */
public class VoidDef extends ASTTypeDef {

	public static final VoidDef INSTANCE = new VoidDef();

	private VoidDef() {
		super(MinefunkParserTreeConstants.JJTTYPEDEF);
		value = new ASTNodeValue(0, 0, 0, 0);
	}

}
