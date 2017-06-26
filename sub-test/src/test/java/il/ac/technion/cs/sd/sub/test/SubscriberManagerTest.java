package il.ac.technion.cs.sd.sub.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.sub.app.SubscriberInitializer;
import il.ac.technion.cs.sd.sub.app.SubscriberReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class SubscriberManagerTest {

  @Rule public Timeout globalTimeout = Timeout.seconds(30);

  private static Injector setupAndGetInjector(String fileName) throws Exception {
      String fileContents =
        new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    Injector injector = Guice.createInjector(new SubscriberModule());//TODO for debug only, new LineStorageModule());
    SubscriberInitializer si = injector.getInstance(SubscriberInitializer.class);
    CompletableFuture<Void> setup =
        fileName.endsWith("csv") ? si.setupCsv(fileContents) : si.setupJson(fileContents);
    setup.get();
    return injector;
  }

  @Test
  public void isWasSubscribedTest() throws Exception {
	  String [] testFiles = { "SubscriberTest.csv", "SubscriberTest.json" };
	  
	  for (String testFile : testFiles) {
		    Injector injector = setupAndGetInjector(testFile);
		    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
		    
		    /* isSubscribed */
		    assertTrue(reader.isSubscribed("journal1", "user1").get().get());
		    assertTrue(reader.isSubscribed("journal4", "user4").get().get());
		    assertFalse(reader.isSubscribed("journal1", "user2").get().get()); // canceled
		    assertTrue(reader.isSubscribed("journal1", "user3").get().get()); // canceled and registered again
		    assertEquals(Optional.empty(), reader.isSubscribed("notExist", "userNE").get());
		    assertEquals(Optional.empty(), reader.isSubscribed("journal1", "userNE").get());
		    assertEquals(Optional.empty(), reader.isSubscribed("notExist", "user1").get());

		    /* wasSubscribed */
		    assertTrue(reader.wasSubscribed("journal1", "user1").get().get());
		    assertTrue(reader.wasSubscribed("journal4", "user4").get().get());
		    assertTrue(reader.wasSubscribed("journal1", "user2").get().get()); // canceled
		    assertTrue(reader.wasSubscribed("journal1", "user3").get().get()); // canceled and registered again
		    assertFalse(reader.wasSubscribed("journal5", "user2").get().get()); // only canceled
		    assertEquals(Optional.empty(), reader.wasSubscribed("notExist", "userNE").get());
		    assertEquals(Optional.empty(), reader.wasSubscribed("journal1", "userNE").get());
		    assertEquals(Optional.empty(), reader.wasSubscribed("notExist", "user1").get());
	  }
  }  
  
  @Test
  public void isWasCanceledTest() throws Exception {
	  String [] testFiles = { "SubscriberTest.csv", "SubscriberTest.json" };
	  
	  for (String testFile : testFiles) {
		    Injector injector = setupAndGetInjector(testFile);
		    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
		    
		    /* isCanceled */
		    assertFalse(reader.isCanceled("journal1", "user1").get().get());
		    assertFalse(reader.isCanceled("journal4", "user4").get().get());
		    assertTrue(reader.isCanceled("journal1", "user2").get().get()); // canceled
		    assertFalse(reader.isCanceled("journal1", "user3").get().get()); // canceled and registered again
		    assertEquals(Optional.empty(), reader.isCanceled("notExist", "userNE").get());
		    assertEquals(Optional.empty(), reader.isCanceled("journal1", "userNE").get());
		    assertEquals(Optional.empty(), reader.isCanceled("notExist", "user1").get());

		    /* wasCanceled */
		    assertFalse(reader.wasCanceled("journal1", "user1").get().get());
		    assertFalse(reader.wasCanceled("journal4", "user4").get().get());
		    assertTrue(reader.wasCanceled("journal1", "user2").get().get()); // canceled
		    assertTrue(reader.wasCanceled("journal1", "user3").get().get()); // canceled and registered again
		    assertFalse(reader.wasCanceled("journal5", "user2").get().get()); // only canceled
		    assertEquals(Optional.empty(), reader.wasCanceled("notExist", "userNE").get());
		    assertEquals(Optional.empty(), reader.wasCanceled("journal1", "userNE").get());
		    assertEquals(Optional.empty(), reader.wasCanceled("notExist", "user1").get());
	  }
  }  
}
