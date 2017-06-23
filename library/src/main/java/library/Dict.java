package library;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import il.ac.technion.cs.sd.sub.ext.*;

/**
 * Creates one {@link FutureLineStorage} Implements a basic dictionary using a
 * {@link FutureLineStorage} and binary search. Allows adding values until a
 * {@link #store()} is performed, after which data is stored persistently and
 * additional adds will not affect the storage.<br>
 * Since only {@link FutureLineStorage} can be used to store data persistently,
 * nothing stops you from calling {@link #store()} twice. However, that would
 * corrupt the {@link Dict}, so don't do that. For the same reason, you can also
 * read from the {@link Dict} before calling {@link #store()} or before it is
 * finished, but that as well may cause errors so watch our for that.
 */
public interface Dict {

	/**
	 * Performs the persistent write using the {@link LineStorage}, and prevents
	 * further writes to the {@link Dict} Don't call this more than once, since
	 * that might corrupt the {@link Dict}.
	 * 
	 * @return a {@link CompletableFuture} that shows whether the store was
	 *         completed. User must make sure to wait on this before calling
	 *         {@link #find()} from a different instance of the same {@link Dict} (i.e same name)
	 */
	public CompletableFuture<Boolean> store();

	/**
	 * adds a pair to the Dict. Should only be called before a store operation
	 * Does not save the data persistently.
	 */
	public void add(String key, String value);

	/**
	 * adds pairs to the Dict. Should only be called before a store operation
	 * Does not save the data persistently.
	 */
	public void addAll(Map<String, String> m);

	/**
	 * @param key
	 *            the key to be searched in the dictionary
	 * @return the value that matches key or {@link Optional.empty} otherwise.
	 */
	public CompletableFuture<Optional<String>> find(String key);
}