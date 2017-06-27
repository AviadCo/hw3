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

  //@Rule public Timeout globalTimeout = Timeout.seconds(30);

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
		    assertTrue(reader.isSubscribed( "user1", "journal1").get().get());
		    assertTrue(reader.isSubscribed("user4", "journal4").get().get());
		    assertFalse(reader.isSubscribed("user2", "journal1").get().get()); // canceled
		    assertTrue(reader.isSubscribed("user3", "journal1").get().get()); // canceled and registered again
		    assertEquals(Optional.empty(), reader.isSubscribed("userNE", "notExist").get());
		    assertEquals(Optional.empty(), reader.isSubscribed("userNE", "journal1").get());
		    assertFalse(reader.isSubscribed("user1", "notExist").get().get());

		    /* wasSubscribed */
		    assertTrue(reader.wasSubscribed("user1", "journal1").get().get());
		    assertTrue(reader.wasSubscribed("user4", "journal4").get().get());
		    assertTrue(reader.wasSubscribed("user2", "journal1").get().get()); // canceled
		    assertTrue(reader.wasSubscribed("user3", "journal1").get().get()); // canceled and registered again
		    assertFalse(reader.wasSubscribed("user2", "journal5").get().get()); // only canceled
		    assertEquals(Optional.empty(), reader.wasSubscribed("userNE", "notExist").get());
		    assertEquals(Optional.empty(), reader.wasSubscribed("userNE", "journal1").get());
		    assertFalse(reader.wasSubscribed("user1", "notExist").get().get());
	  }
  }  
  
  @Test
  public void isWasCanceledTest() throws Exception {
	  String [] testFiles = { "SubscriberTest.csv", "SubscriberTest.json" };
	  
	  for (String testFile : testFiles) {
		    Injector injector = setupAndGetInjector(testFile);
		    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
		    
		    /* isCanceled */
		    assertFalse(reader.isCanceled("user1", "journal1").get().get());
		    assertFalse(reader.isCanceled("user4", "journal4").get().get());
		    assertTrue(reader.isCanceled("user2", "journal1").get().get()); // canceled
		    assertFalse(reader.isCanceled("user3", "journal1").get().get()); // canceled and registered again
		    assertEquals(Optional.empty(), reader.isCanceled( "userNE", "notExist").get());
		    assertEquals(Optional.empty(), reader.isCanceled("userNE", "journal1").get());
		    assertFalse(reader.isCanceled("user1", "notExist").get().get());

		    /* wasCanceled */
		    assertFalse(reader.wasCanceled("user1", "journal1").get().get());
		    assertFalse(reader.wasCanceled("user4", "journal4").get().get());
		    assertTrue(reader.wasCanceled("user2", "journal1").get().get()); // canceled
		    assertTrue(reader.wasCanceled("user3", "journal1").get().get()); // canceled and registered again
		    assertFalse(reader.wasCanceled("user2", "journal5").get().get()); // only canceled
		    assertEquals(Optional.empty(), reader.wasCanceled("userNE", "notExist").get());
		    assertEquals(Optional.empty(), reader.wasCanceled("userNE", "journal1").get());
		    assertFalse(reader.wasCanceled("user1", "notExist").get().get());
	  }
  }  
}
