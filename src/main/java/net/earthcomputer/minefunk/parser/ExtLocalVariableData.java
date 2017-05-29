package net.earthcomputer.minefunk.parser;

/**
 * A class containing extra local variable data for a frame in the index
 * 
 * @author Earthcomputer
 */
public class ExtLocalVariableData {

	private Object constValue;

	public ExtLocalVariableData(Object constValue) {
		this.constValue = constValue;
	}

	/**
	 * Sets the constant value of the local variable
	 * 
	 * @param constValue
	 */
	public void setConstValue(Object constValue) {
		this.constValue = constValue;
	}

	/**
	 * Gets the constant value of the local variable, or <tt>null</tt> if the
	 * variable is not constant
	 * 
	 * @return
	 */
	public Object getConstValue() {
		return constValue;
	}

}
