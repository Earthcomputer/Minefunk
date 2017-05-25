package net.earthcomputer.minefunk.parser;

public class ExtLocalVariableData {

	private Object constValue;
	
	public ExtLocalVariableData(Object constValue) {
		this.constValue = constValue;
	}
	
	public void setConstValue(Object constValue) {
		this.constValue = constValue;
	}
	
	public Object getConstValue() {
		return constValue;
	}
	
}
