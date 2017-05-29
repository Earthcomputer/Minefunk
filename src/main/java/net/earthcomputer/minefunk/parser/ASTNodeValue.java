package net.earthcomputer.minefunk.parser;

/**
 * The value in an AST node. All AST nodes value field must contain an instance
 * of this class. This object contains information such as the position of the
 * AST node in the original source file, and any custom value.
 * 
 * @author Earthcomputer
 * @see SimpleNode#value
 */
public class ASTNodeValue {

	private int startLine;
	private int startCol;
	private int endLine;
	private int endCol;
	private Object value;

	/**
	 * Creates an <tt>ASTNodeValue</tt> with the given range in the source code
	 * with no custom value
	 * 
	 * @param startLine
	 *            - the start line in the source code
	 * @param startCol
	 *            - the start column in the source code
	 * @param endLine
	 *            - the end line in the source code
	 * @param endCol
	 *            - the end column in the source code
	 */
	public ASTNodeValue(int startLine, int startCol, int endLine, int endCol) {
		this(startLine, startCol, endLine, endCol, null);
	}

	/**
	 * Creates an <tt>ASTNodeValue</tt> with the given range in the source code
	 * with the given custom value
	 * 
	 * @param startLine
	 *            - the start line in the source code
	 * @param startCol
	 *            - the start column in the source code
	 * @param endLine
	 *            - the end line in the source code
	 * @param endCol
	 *            - the end column in the source code
	 * @param value
	 *            - the custom value
	 */
	public ASTNodeValue(int startLine, int startCol, int endLine, int endCol, Object value) {
		this.startLine = startLine;
		this.startCol = startCol;
		this.endLine = endLine;
		this.endCol = endCol;
		this.value = value;
	}

	/**
	 * Gets the start line in the source code
	 * 
	 * @return The start line in the source code
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * Gets the start column in the source code
	 * 
	 * @return The start column in the source code
	 */
	public int getStartColumn() {
		return startCol;
	}

	/**
	 * Gets the end line in the source code
	 * 
	 * @return The end line in the source code
	 */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * Gets the end column in the source code
	 * 
	 * @return The end column in the source code
	 */
	public int getEndColumn() {
		return endCol;
	}

	/**
	 * Gets the custom value, or <tt>null</tt> if none was set
	 * 
	 * @return The custom value
	 */
	public Object getValue() {
		return value;
	}

}
