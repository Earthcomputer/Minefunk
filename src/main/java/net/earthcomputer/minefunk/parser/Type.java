package net.earthcomputer.minefunk.parser;

import java.util.Collections;
import java.util.List;

public class Type {

	public static final Type BOOL = new Type("bool");
	public static final Type INT = new Type("int");
	public static final Type STRING = new Type("string");
	public static final Type VOID = new Type("void");

	private List<String> namespaces;
	private String typeName;

	public Type(String typeName) {
		this(Collections.emptyList(), typeName);
	}

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

	public boolean isBool() {
		return namespaces.isEmpty() && "bool".equals(typeName);
	}
	
	public boolean isInt() {
		return namespaces.isEmpty() && "int".equals(typeName);
	}
	
	public boolean isString() {
		return namespaces.isEmpty() && "string".equals(typeName);
	}
	
	public boolean isVoid() {
		return namespaces.isEmpty() && "void".equals(typeName);
	}

	@Override
	public int hashCode() {
		return typeName.hashCode() + 31 * namespaces.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (other == null) {
			return false;
		} else if (other.getClass() != Type.class) {
			return false;
		} else {
			Type type = (Type) other;
			return typeName.equals(type.typeName) && namespaces.equals(type.namespaces);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		namespaces.forEach(ns -> sb.append(ns).append("::"));
		return sb.append(typeName).toString();
	}

}
