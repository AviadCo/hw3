package library;

import com.google.inject.AbstractModule;

import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;
import library.TestStorerFactory;

public class TestLineStorageModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FutureLineStorageFactory.class).to(TestStorerFactory.class);
		bind(FutureLineStorage.class).to(TestStorer.class);
	}

}
