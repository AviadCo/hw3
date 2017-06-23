package library;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

	@Inject
	DictImpl(FutureLineStorageFactory factory, //
			@Assisted String name) {
		storer = factory.open(name);
		storingStatus = storer;
	}

	public CompletableFuture<Boolean> store() {
		(storingStatus = storeToStorage(pairs, storer, storer)).thenAccept(s -> {
		});
		
		return CompletableFuture.completedFuture(true);
	}

	static CompletableFuture<?> storeToStorage(Map<String, String> map, CompletableFuture<Optional<FutureLineStorage>> store,
			CompletableFuture<?> current) {
		for (String key : map.keySet().stream().sorted().collect(Collectors.toList())) {
			current = current.thenCompose(v -> store.thenCompose(s -> s.isPresent() ? s.get().appendLine(key) : CompletableFuture.completedFuture(false)));
			current = current.thenCompose(v -> store.thenCompose(s -> s.isPresent() ? s.get().appendLine(map.get(key)) : CompletableFuture.completedFuture(false)));
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

	@Override
	public CompletableFuture<Optional<String>> find(String key) {
		return storingStatus
				.thenCompose(v -> BinarySearch.valueOf(storer, key, 0, storer.thenCompose(s -> s.get().numberOfLines())));
	}
}
