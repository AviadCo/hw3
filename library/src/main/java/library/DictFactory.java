package library;

import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;

/**
 * A factory of {@link Dict}. Use guice and {@link LibraryModule} to get an
 * instance of the factory and create a {@link Dict}
 */
public interface DictFactory {

	/**
	 * @param name
	 *            the name of the {@link Dict}. to avoid collision, give each
	 *            distinct {@link Dict} and {@link DoubleKeyDict} its own name,
	 *            made up only of letters.
	 * @return a {@link Dict} using a {@link FutureLineStorage} whose name is
	 *         name
	 */
	public Dict create(String name);
}