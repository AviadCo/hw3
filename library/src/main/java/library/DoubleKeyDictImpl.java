package library;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;

/**
 * The provided implementation of {@link DoubleKeyDict}, using
 * {@link FutureLineStorage}
 * 
 * @see {@link DoubleKeyDictFactory} and {@link LibraryModule} for more info on
 *      how to create an instance
 */
public class DoubleKeyDictImpl implements DoubleKeyDict {
	private final Dict mainKeyDict;
	private final Dict secondaryKeyDict;
	private CompletableFuture<?> storingStatus;
	private final CompletableFuture<Optional<FutureLineStorage>> storer;
	private final Map<String, Map<String, String>> mainKeyMap = new HashMap<>();
	private final Map<String, Map<String, String>> secondaryKeyMap = new HashMap<>();

	private class IntegerWrapper {
		public int val;

		public IntegerWrapper() {
			this.val = 0;
		}

		public String toString() {
			return val + "";
		}
	}

	@Inject
	public DoubleKeyDictImpl(DictFactory dictFactory, FutureLineStorageFactory lineStorageFactory,
			@Assisted String name) {
		storer = lineStorageFactory.open(name + ".valuesMap");
		storingStatus = storer;
		mainKeyDict = dictFactory.create(name + ".mainKeyMap");
		secondaryKeyDict = dictFactory.create(name + ".secondaryKeyMap");
	}

	@Override
	public void add(String mainKey, String secondaryKey, String value) {
		addToMap(mainKeyMap, mainKey, secondaryKey, value);
		addToMap(secondaryKeyMap, secondaryKey, mainKey, value);
	}

	public CompletableFuture<Boolean> store() {
		IntegerWrapper currentLine = new IntegerWrapper();
		storingStatus = storeDict(mainKeyDict, mainKeyMap, currentLine, storingStatus);
		storingStatus = storeDict(secondaryKeyDict, secondaryKeyMap, currentLine, storingStatus);

		//TODO check if it's always true..
		return CompletableFuture.completedFuture(true);
	}

	private CompletableFuture<?> storeDict(final Dict dict, final Map<String, Map<String, String>> m,
			IntegerWrapper currentLine, CompletableFuture<?> currentStatus) {
		CompletableFuture<?> status = currentStatus;
		for (String key : m.keySet()) {
			Map<String, String> current = m.get(key);
			status = DictImpl.storeToStorage(current, storer, status);
			int startingLine = currentLine.val;
			currentLine.val += current.size() * 2;
			dict.add(key, startingLine + "," + currentLine);
		}
		dict.store();
		return status;
	}

	private void addToMap(final Map<String, Map<String, String>> m, String key1, String key2, String value) {
		if (!m.containsKey(key1))
			m.put(key1, new HashMap<>());
		m.get(key1).put(key2, value);
	}

	@Override
	public CompletableFuture<Map<String, String>> findByMainKey(String key) {
		return findByKey(mainKeyDict, key);
	}

	@Override
	public CompletableFuture<Optional<String>> findByKeys(String key1, String key2) {
		return mainKeyDict.find(key1).thenCompose(o -> {
			if (!o.isPresent())
				return CompletableFuture.completedFuture(Optional.empty());
			String[] lines = o.get().split(",");
			return storingStatus.thenCompose(
					v -> BinarySearch.valueOf(storer, key2, Integer.parseInt(lines[0]), Integer.parseInt(lines[1])));
		});
	}

	@Override
	public CompletableFuture<Map<String, String>> findBySecondaryKey(String key) {
		return findByKey(secondaryKeyDict, key);
	}

	private CompletableFuture<Map<String, String>> findByKey(Dict d, String key) {
		return d.find(key).thenApply(o -> o.map(str -> {
			Map<String, String> $ = new HashMap<>();
			
			if (!o.isPresent()) {
				return $;
			}
			
			String[] lines = o.get().split(",");
			int end = Integer.parseInt(lines[1]);
			try {
				storingStatus.get();
				for (int i = Integer.parseInt(lines[0]); i < end; i += 2) {
					final int line = i;

					$.put(storer.thenCompose(s -> s.get().read(line)).get().get(),
						  storer.thenCompose(s -> s.get().read(line + 1)).get().get());
				}
			} catch (InterruptedException | ExecutionException e) {
				return new HashMap<String, String>();
			}
			return $;
		}).orElse(new HashMap<String, String>()));
	}
}
