package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing extra function data for the index
 * 
 * @author Earthcomputer
 */
public class ExtFunctionData {

	private List<String> namespaces;
	private String id;

	public ExtFunctionData(List<String> namespaces) {
		this.namespaces = new ArrayList<>(namespaces);
	}

	/**
	 * Gets the namespaces of the function
	 * 
	 * @return The namespaces of the function
	 */
	public List<String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Gets the final name of the function, or <tt>null</tt> if the index hasn't
	 * generated one yet
	 * 
	 * @return The final name of the function
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the final name of the function
	 * 
	 * @param id
	 *            - the final name of the function
	 */
	public void setId(String id) {
		this.id = id;
	}

}
