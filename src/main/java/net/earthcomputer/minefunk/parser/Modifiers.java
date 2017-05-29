package net.earthcomputer.minefunk.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * A class containing constants for the modifiers of types, variables and
 * functions
 * 
 * @author Earthcomputer
 */
public class Modifiers {

	private Modifiers() {
	}

	/**
	 * The names of all the modifiers
	 */
	private static final Map<Integer, String> modifierNames = new HashMap<>();

	/**
	 * This pseudo-modifier represents when something has no modifiers
	 */
	public static final int NONE = 0;
	/**
	 * This modifier goes on functions. If present, the function will not be
	 * generated as a separate function file but will be inlined into the
	 * caller.
	 */
	public static final int INLINE = 1;
	/**
	 * This modifier goes on variables. If present, the variable must have an
	 * initializer and cannot be modified afterwards.
	 */
	public static final int CONST = 2;

	/**
	 * Flags containing all modifiers allowed on type definitions
	 */
	public static final int ALLOWED_TYPE_MODIFIERS = NONE;
	/**
	 * Flags containing all modifiers allowed on field definitions
	 */
	public static final int ALLOWED_VARIABLE_MODIFIERS = CONST;
	/**
	 * Flags containing all modifiers allowed on function definitions
	 */
	public static final int ALLOWED_FUNCTION_MODIFIERS = INLINE;

	static {
		modifierNames.put(INLINE, "inline");
		modifierNames.put(CONST, "const");
	}

	/**
	 * Converts the given modifier flags to a string
	 * 
	 * @param modifiers
	 *            - the modifier flags
	 * @return A string representation
	 */
	public static String toString(int modifiers) {
		if (modifiers == NONE) {
			return "none";
		}
		StringBuilder str = new StringBuilder();
		modifierNames.forEach((mask, name) -> {
			if ((modifiers & mask) != 0) {
				if (str.length() != 0) {
					str.append(" ");
				}
				str.append(name);
			}
		});
		return str.toString();
	}

}
