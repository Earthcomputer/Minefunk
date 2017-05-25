package net.earthcomputer.minefunk.parser;

import java.util.ArrayList;
import java.util.List;

public class ExtFunctionData {

	private List<String> namespaces;
	private String id;
	
	public ExtFunctionData(List<String> namespaces) {
		this.namespaces = new ArrayList<>(namespaces);
	}
	
	public List<String> getNamespaces() {
		return namespaces;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
}
