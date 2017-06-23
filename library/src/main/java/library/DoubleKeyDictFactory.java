package library;

import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;

/**
 * A factory of {@link DoubleKeyDict}. Use guice and {@link LibraryModule} to
 * get an instance of the factory and create a {@link DoubleKeyDict}
 */
public interface DoubleKeyDictFactory {

	/**
	 * @param name
	 *            the name of the {@link DoubleKeyDict}. to avoid collision,
	 *            give each {@link Dict} and {@link DoubleKeyDict} its own name,
	 *            made up only of letters.
	 * @return a {@link DoubleKeyDict} using {@link FutureLineStorage}'s whose
	 *         names are <br>
	 *         {@code name + "." + additionalString}
	 */
	public DoubleKeyDict create(String name);
}
