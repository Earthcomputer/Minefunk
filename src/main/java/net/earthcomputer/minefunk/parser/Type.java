package net.earthcomputer.minefunk.parser;

import java.util.List;

public class Type {

	private List<String> namespaces;
	private String typeName;
	
	public Type(List<String> namespaces, String typeName) {
		this.namespaces = namespaces;
		this.typeName = typeName;
	}

	public List<String> getNamespaces() {
		return namespaces;
	}

	public String getTypeName() {
		return typeName;
	}
	
}
