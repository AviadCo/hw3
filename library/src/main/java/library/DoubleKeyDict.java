package library;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;

/**
 * creates three {@link FutureLineStorage} Implements a mapping from pairs of
 * keys using a {@link FutureLineStorage} and binary search. Allows adding
 * values until a {@link #store()} is performed, after which data is stored
 * persistently and additional adds will not affect the storage. <br>
 * Since only {@link FutureLineStorage} can be used to store data persistently,
 * nothing stops you from calling {@link #store()} twice. However, that would
 * corrupt the {@link DoubleKeyDict}, so don't do that. For the same reason, you
 * can also read from the {@link DoubleKeyDict} before calling {@link #store()} or
 * before it is finished, but that as well may cause errors so watch our for
 * that.
 */
public interface DoubleKeyDict {

	/**
	 * adds a new mapping of two keys to value to the {@link DoubleKeyDict}.
	 * Should only be called before a store operation Does not save the data
	 * persistently.
	 */
	public void add(String mainKey, String secondaryKey, String value);

	/**
	 * Performs the persistent write using {@link FutureLineStorage}
	 * 
	 * @return a {@link CompletableFuture} that shows whether the store was
	 *         completed. User must make sure to wait on this before calling
	 *         any find method from a different instance of the same
	 *         {@link DoubleKeyDict} (i.e same name)
	 *         @see {@link #findByMainKey}, {@link #findBySecondaryKey}, {@link #findByKeys}
	 */
	public CompletableFuture<Boolean> store();

	/**
	 * A search by the main key
	 * 
	 * @param key
	 * @return mapping from all secondary keys which match the main key to the
	 *         value the two keys represent.
	 */
	public CompletableFuture<Map<String, String>> findByMainKey(String mainKey);

	/**
	 * a search by two keys
	 * 
	 * @param mainKey
	 * @param secondaryKey
	 * @return the value saved by both keys, or {@link Optional#empty()} if no
	 *         such combination of keys exists.
	 */
	public CompletableFuture<Optional<String>> findByKeys(String mainKey, String secondaryKey);

	/**
	 * A search by the secondary key
	 * 
	 * @param key
	 * @return mapping from all main keys which match the secondary key to the
	 *         value the two keys represent.
	 */
	public CompletableFuture<Map<String, String>> findBySecondaryKey(String secondaryKey);
}
