package library;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;

/**
 * The provided implementation of {@link Dict}, using {@link FutureLineStorage}
 * 
 * @see {@link DictFactory} and {@link LibraryModule} for more info on how to
 *      create an instance
 */
public class DictImpl implements Dict {
	private final CompletableFuture<Optional<FutureLineStorage>> storer;
	private final Map<String, String> pairs = new HashMap<>();
	private CompletableFuture<?> storingStatus;

	private CompletableFuture<Optional<FutureLineStorage>> recursiveOpen(FutureLineStorageFactory factory, String name) {
		CompletableFuture<Optional<FutureLineStorage>> futureLineStorage = factory.open(name);
		
		return futureLineStorage.thenCompose(f -> f.isPresent() ? futureLineStorage : recursiveOpen(factory, name));
	}
	
	@Inject
	DictImpl(FutureLineStorageFactory factory, //
			@Assisted String name) {
		storer = recursiveOpen(factory, name);
		storingStatus = storer;
	}
		
	public CompletableFuture<Boolean> store() {
		(storingStatus = storeToStorage(pairs, storer, storer)).thenAccept(s -> {
		});
				
		return CompletableFuture.completedFuture(true);
	}

	private static CompletableFuture<Boolean> recursiveAppendLine(Optional<FutureLineStorage> store, String toStore) {
		CompletableFuture<Boolean> result = store.get().appendLine(toStore);
		
		return result.thenCompose(res -> res ? result : recursiveAppendLine(store, toStore));
	}
	
	static CompletableFuture<?> storeToStorage(Map<String, String> map, CompletableFuture<Optional<FutureLineStorage>> store,
			CompletableFuture<?> current) {
		for (String key : map.keySet().stream().sorted().collect(Collectors.toList())) {
			current = current.thenCompose(v -> store.thenCompose(s -> recursiveAppendLine(s, key)));
			current = current.thenCompose(v -> store.thenCompose(s -> recursiveAppendLine(s, map.get(key))));
		}
				
		return current;
	}

	@Override
	public void add(String key, String value) {
		pairs.put(key, value);
	}

	@Override
	public void addAll(Map<String, String> ps) {
		pairs.putAll(ps);
	}

	private static CompletableFuture<OptionalInt> recursiveNumberOfLines(Optional<FutureLineStorage> store) {
		CompletableFuture<OptionalInt> result = store.get().numberOfLines();
		
		return result.thenCompose(res -> res.isPresent() ? result : recursiveNumberOfLines(store));
	}
	
	@Override
	public CompletableFuture<Optional<String>> find(String key) {
		return storingStatus
				.thenCompose(v -> BinarySearch.valueOf(storer, key, 0, storer.thenCompose(s -> recursiveNumberOfLines(s))));
	}
}
