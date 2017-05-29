package net.earthcomputer.minefunk.parser;

import java.util.Collections;
import java.util.List;

/**
 * Represents a name within namespaces, e.g. a type, variable or function name,
 * or unresolved references of each of these
 * 
 * @author Earthcomputer
 */
public class Type {

	/**
	 * The built-in boolean type
	 */
	public static final Type BOOL = new Type("bool");
	/**
	 * The built-in integer type
	 */
	public static final Type INT = new Type("int");
	/**
	 * The built-in string type
	 */
	public static final Type STRING = new Type("string");
	/**
	 * The built-in void type
	 */
	public static final Type VOID = new Type("void");

	private List<String> namespaces;
	private String typeName;

	/**
	 * Creates a type with no namespaces. Unless this is a built-in type, this
	 * will be unqualified.
	 * 
	 * @param typeName
	 *            - the type name
	 */
	public Type(String typeName) {
		this(Collections.emptyList(), typeName);
	}

	/**
	 * Creates a type with the given namespaces and the given name.
	 * 
	 * @param namespaces
	 *            - the namespaces
	 * @param typeName
	 *            - the name
	 */
	public Type(List<String> namespaces, String typeName) {
		this.namespaces = namespaces;
		this.typeName = typeName;
	}

	/**
	 * Gets the namespaces of this type
	 * 
	 * @return The namespaces of this type
	 */
	public List<String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Gets the name of this type
	 * 
	 * @return The name of this type
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Gets whether this type represents the built-in boolean type
	 * 
	 * @return Whether this type is "bool"
	 */
	public boolean isBool() {
		return namespaces.isEmpty() && "bool".equals(typeName);
	}

	/**
	 * Gets whether this type represents the built-in integer type
	 * 
	 * @return Whether this type is "int"
	 */
	public boolean isInt() {
		return namespaces.isEmpty() && "int".equals(typeName);
	}

	/**
	 * Gets whether this type represents the built-in string type
	 * 
	 * @return Whether this type is "string"
	 */
	public boolean isString() {
		return namespaces.isEmpty() && "string".equals(typeName);
	}

	/**
	 * Gets whether this type represents the built-in void type
	 * 
	 * @return Whether this type is "void"
	 */
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
