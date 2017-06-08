package net.earthcomputer.minefunk.parser;

import java.util.List;

/**
 * A class which contains all the custom data keys for AST nodes
 * 
 * @author Earthcomputer
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Keys {

	private Keys() {
	}

	public static final UserDataKey<Integer> ID = new UserDataKey<>(Integer.class);
	public static final UserDataKey<List<String>> NAMESPACES = (UserDataKey<List<String>>) (UserDataKey<?>) new UserDataKey<List>(
			List.class);
	public static final UserDataKey<Object> CONST_VALUE = new UserDataKey<>(Object.class);

}