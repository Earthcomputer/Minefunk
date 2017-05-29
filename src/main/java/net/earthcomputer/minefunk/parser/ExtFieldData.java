package net.earthcomputer.minefunk.parser;

import java.util.List;

/**
 * A class containing extra field data for the index
 * 
 * @author Earthcomputer
 */
public class ExtFieldData {

	private List<String> namespaces;

	public ExtFieldData(List<String> namespaces) {
		this.namespaces = namespaces;
	}

	/**
	 * Gets the namespaces of the field
	 * 
	 * @return The namespaces of the field
	 */
	public List<String> getNamespaces() {
		return namespaces;
	}

}
