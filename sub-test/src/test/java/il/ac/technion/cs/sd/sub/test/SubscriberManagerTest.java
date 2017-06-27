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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class SubscriberManagerTest {

  //@Rule public Timeout globalTimeout = Timeout.seconds(50);

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
		    assertFalse(reader.isSubscribed("userNE", "notExist").get().get());
		    assertFalse(reader.isSubscribed("userNE", "journal1").get().get());
		    assertFalse(reader.isSubscribed("user1", "notExist").get().get());

		    /* wasSubscribed */
		    assertTrue(reader.wasSubscribed("user1", "journal1").get().get());
		    assertTrue(reader.wasSubscribed("user4", "journal4").get().get());
		    assertTrue(reader.wasSubscribed("user2", "journal1").get().get()); // canceled
		    assertTrue(reader.wasSubscribed("user3", "journal1").get().get()); // canceled and registered again
		    assertFalse(reader.wasSubscribed("user2", "journal5").get().get()); // only canceled
		    assertFalse(reader.wasSubscribed("userNE", "notExist").get().get());
		    assertFalse(reader.wasSubscribed("userNE", "journal1").get().get());
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
		    assertFalse(reader.isCanceled( "userNE", "notExist").get().get());
		    assertFalse(reader.isCanceled("userNE", "journal1").get().get());
		    assertFalse(reader.isCanceled("user1", "notExist").get().get());

		    /* wasCanceled */
		    assertFalse(reader.wasCanceled("user1", "journal1").get().get());
		    assertFalse(reader.wasCanceled("user4", "journal4").get().get());
		    assertTrue(reader.wasCanceled("user2", "journal1").get().get()); // canceled
		    assertTrue(reader.wasCanceled("user3", "journal1").get().get()); // canceled and registered again
		    assertFalse(reader.wasCanceled("user2", "journal5").get().get()); // only canceled
		    assertFalse(reader.wasCanceled("userNE", "notExist").get().get());
		    assertFalse(reader.wasCanceled("userNE", "journal1").get().get());
		    assertFalse(reader.wasCanceled("user1", "notExist").get().get());
	  }
  }  
  
  @Test
  public void getSubscribedJournalsAndGetAllSubscriptionsTest() throws Exception {
	  String [] testFiles = { "SubscriberTest.csv", "SubscriberTest.json" };
	  
	  for (String testFile : testFiles) {
		    Injector injector = setupAndGetInjector(testFile);
		    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
		    
		    /* getSubscribedJournals */
		    assertEquals(Arrays.asList(), reader.getSubscribedJournals( "userNE").get());
		    assertEquals(Arrays.asList("journal1", "journal4"), reader.getSubscribedJournals( "user1").get());
		    assertEquals(Arrays.asList("journal2", "journal3"), reader.getSubscribedJournals( "user2").get());
		    assertEquals(Arrays.asList("journal1", "journal3"), reader.getSubscribedJournals( "user3").get());
		    assertEquals(Arrays.asList("journal1", "journal4"), reader.getSubscribedJournals( "user4").get());
		    assertEquals(Arrays.asList("journal1", "journal3"), reader.getSubscribedJournals( "user5").get());

		    /* getAllSubscriptions */
		    Map<String, List<Boolean>> map1 = new HashMap<String, List<Boolean>>();
		    map1.put("journal1", Arrays.asList(true));
		    map1.put("journal4", Arrays.asList(true));
		    assertEquals(map1, reader.getAllSubscriptions("user1").get());
		    
		    Map<String, List<Boolean>> map2 = new HashMap<String, List<Boolean>>();
		    map2.put("journal1", Arrays.asList(true, false));
		    map2.put("journal2", Arrays.asList(true, true));
		    map2.put("journal3", Arrays.asList(true));
		    map2.put("journal5", Arrays.asList(false));
		    assertEquals(map2, reader.getAllSubscriptions("user2").get());
		    
		    Map<String, List<Boolean>> map3 = new HashMap<String, List<Boolean>>();
		    map3.put("journal1", Arrays.asList(true, false, true));
		    map3.put("journal3", Arrays.asList(true, true, true, true));
		    assertEquals(map3, reader.getAllSubscriptions("user3").get());
		    
		    Map<String, List<Boolean>> map4 = new HashMap<String, List<Boolean>>();
		    map4.put("journal1", Arrays.asList(true, false, true, true));
		    map4.put("journal4", Arrays.asList(true, true));
		    assertEquals(map4, reader.getAllSubscriptions("user4").get());
		    
		    Map<String, List<Boolean>> map5 = new HashMap<String, List<Boolean>>();
		    map5.put("journal1", Arrays.asList(true));
		    map5.put("journal3", Arrays.asList(true));
		    assertEquals(map5, reader.getAllSubscriptions("user5").get());
	  }
  } 
  
  @Test
  public void getMonthlyBudgetGetMonthlyIncomeTest() throws Exception {
	  String [] testFiles = { "SubscriberTest.csv", "SubscriberTest.json" };
	  
	  for (String testFile : testFiles) {
		    Injector injector = setupAndGetInjector(testFile);
		    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
		    
		    /* getMonthlyBudget */
		    assertEquals(OptionalInt.of(0), reader.getMonthlyBudget("userNE").get());
		    assertEquals(OptionalInt.of(170), reader.getMonthlyBudget("user1").get());
		    assertEquals(OptionalInt.of(710), reader.getMonthlyBudget("user2").get());
		    assertEquals(OptionalInt.of(160), reader.getMonthlyBudget("user3").get());
		    assertEquals(OptionalInt.of(170), reader.getMonthlyBudget("user4").get());
		    assertEquals(OptionalInt.of(160), reader.getMonthlyBudget("user5").get());

		    /* getMonthlyIncome */
		    assertEquals(OptionalInt.empty(), reader.getMonthlyIncome("NotExist").get());
		    assertEquals(OptionalInt.of(750), reader.getMonthlyIncome("journal1").get());
		    assertEquals(OptionalInt.of(700), reader.getMonthlyIncome("journal2").get());
		    assertEquals(OptionalInt.of(30), reader.getMonthlyIncome("journal3").get());
		    assertEquals(OptionalInt.of(40), reader.getMonthlyIncome("journal4").get());
		    assertEquals(OptionalInt.of(0), reader.getMonthlyIncome("journal5").get());
	  }
  } 
  
  @Test
  public void getSubscribedUsersGetSubscribersTest() throws Exception {
	  String [] testFiles = { "SubscriberTest.csv", "SubscriberTest.json" };
	  
	  for (String testFile : testFiles) {
		    Injector injector = setupAndGetInjector(testFile);
		    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
		    
		    /* getSubscribedUsers */
		    assertEquals(Arrays.asList(), reader.getSubscribedUsers("NotExist").get());
		    assertEquals(Arrays.asList("user1", "user3", "user4", "user5", "user6"), reader.getSubscribedUsers("journal1").get());
		    assertEquals(Arrays.asList("user2"), reader.getSubscribedUsers("journal2").get());
		    assertEquals(Arrays.asList("user2", "user3", "user5"), reader.getSubscribedUsers("journal3").get());
		    assertEquals(Arrays.asList("user1", "user4"), reader.getSubscribedUsers("journal4").get());
		    assertEquals(Arrays.asList(), reader.getSubscribedUsers("journal5").get());

		    /* getSubscribers */
		    Map<String, List<Boolean>> map1 = new HashMap<String, List<Boolean>>();
		    map1.put("user1", Arrays.asList(true));
		    map1.put("user2", Arrays.asList(true, false));
		    map1.put("user3", Arrays.asList(true, false, true));
		    map1.put("user4", Arrays.asList(true, false, true, true));
		    map1.put("user5", Arrays.asList(true));
		    map1.put("user6", Arrays.asList(true));
		    assertEquals(map1, reader.getSubscribers("journal1").get());
		    
		    Map<String, List<Boolean>> map2 = new HashMap<String, List<Boolean>>();
		    map2.put("user2", Arrays.asList(true, true));
		    assertEquals(map2, reader.getSubscribers("journal2").get());
		    
		    Map<String, List<Boolean>> map3 = new HashMap<String, List<Boolean>>();
		    map3.put("user2", Arrays.asList(true));
		    map3.put("user3", Arrays.asList(true, true, true, true));
		    map3.put("user5", Arrays.asList(true));
		    assertEquals(map3, reader.getSubscribers("journal3").get());
		    
		    Map<String, List<Boolean>> map4 = new HashMap<String, List<Boolean>>();
		    map4.put("user1", Arrays.asList(true));
		    map4.put("user4", Arrays.asList(true, true));
		    assertEquals(map4, reader.getSubscribers("journal4").get());
		    
		    Map<String, List<Boolean>> map5 = new HashMap<String, List<Boolean>>();
		    map5.put("user2", Arrays.asList(false));
		    assertEquals(map5, reader.getSubscribers("journal5").get());
	  }
  } 
}
