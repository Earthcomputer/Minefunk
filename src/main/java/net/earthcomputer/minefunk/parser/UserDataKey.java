package net.earthcomputer.minefunk.parser;

/**
 * A user data key
 * 
 * @author Earthcomputer
 *
 * @param <T>
 *            The type of the data stored
 */
public class UserDataKey<T> {

	private Class<T> clazz;

	/**
	 * Creates a user data key with the type of the data stored. We need this
	 * argument to cast to the right class when the user data is obtained.
	 * 
	 * @param clazz
	 */
	public UserDataKey(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Gets the type of the data stored.
	 * 
	 * @return The type of the data stored
	 */
	public Class<T> getClazz() {
		return clazz;
	}

}
