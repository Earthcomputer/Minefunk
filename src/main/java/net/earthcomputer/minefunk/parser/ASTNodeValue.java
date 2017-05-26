package net.earthcomputer.minefunk.parser;

public class ASTNodeValue {

	private int startLine;
	private int startCol;
	private int endLine;
	private int endCol;
	private Object value;
	
	public ASTNodeValue(int startLine, int startCol, int endLine, int endCol) {
		this(startLine, startCol, endLine, endCol, null);
	}
	
	public ASTNodeValue(int startLine, int startCol, int endLine, int endCol, Object value) {
		this.startLine = startLine;
		this.startCol = startCol;
		this.endLine = endLine;
		this.endCol = endCol;
		this.value = value;
	}
	
	public int getStartLine() {
		return startLine;
	}
	
	public int getStartColumn() {
		return startCol;
	}
	
	public int getEndLine() {
		return endLine;
	}
	
	public int getEndColumn() {
		return endCol;
	}
	
	public Object getValue() {
		return value;
	}
	
}
