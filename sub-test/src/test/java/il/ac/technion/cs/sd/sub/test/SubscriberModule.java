package il.ac.technion.cs.sd.sub.test;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;

import il.ac.technion.cs.sd.sub.app.SubscriberInitializer;
import il.ac.technion.cs.sd.sub.app.SubscriberManager;
import il.ac.technion.cs.sd.sub.app.SubscriberReader;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;

import library.Dict;
import library.DictFactory;
import library.DictImpl;
import library.TestLineStorageModule;
import library.TestStorer;
import library.TestStorerFactory;

// This module is in the testing project, so that it could easily bind all dependencies from all levels.
public class SubscriberModule extends AbstractModule {
	
  @Override
  protected void configure() {
	  bind(SubscriberInitializer.class).to(SubscriberManager.class);
	  bind(SubscriberReader.class).to(SubscriberManager.class);
	  
	  install(new FactoryModuleBuilder().implement(Dict.class, DictImpl.class)
				.build(DictFactory.class));
	  	  
	  install(new TestLineStorageModule()); //TODO remember to remove this bind
//	  bind(FutureLineStorageFactory.class).to(TestStorerFactory.class); //TODO remember to remove this bind
//	  bind(FutureLineStorage.class).to(TestStorer.class);
//	  bind(FutureLineStorageFactory.class).to(TestStorerFactory.class);
//	  bind(FutureLineStorage.class).to(TestStorer.class);
  }
	  
	@Provides
	@Singleton 
	@Named(SubscriberManager.USERS_DICT_NAME)
	Dict createUserDict(DictFactory dictFactory) {
		return dictFactory.create(SubscriberManager.USERS_DICT_NAME);
	}
	
	@Provides
	@Singleton 
	@Named(SubscriberManager.JOURNALS_DICT_NAME)
	Dict createJournalDict(DictFactory dictFactory) {
		return dictFactory.create(SubscriberManager.JOURNALS_DICT_NAME);
	}
}
