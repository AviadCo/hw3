package library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class DoubleKeyDictTest {
	static Injector injector = Guice.createInjector(new TestLineStorageModule(), new LibraryModule());
	static DoubleKeyDictFactory f = injector.getInstance(DoubleKeyDictFactory.class);
	
	@Test()
	public void test0() throws InterruptedException, ExecutionException {
		DoubleKeyDict testDict = f.create("test0");
		testDict.add("a", "Dor", "Niv");
		testDict.add("b", "Niv", "10203040");
		testDict.add("b", "Dor", "1020304050");
		
		testDict.store();
		assertEquals(2, testDict.findByMainKey("b").get().size());
	}

	@Test()
	public void test1() throws InterruptedException, ExecutionException {
		DoubleKeyDict testDict = f.create("test1");
		testDict.add("12345", "Dor", "a1");
		testDict.add("54321", "Niv", "a2");
		//
		testDict.store();
		//
		assertTrue(testDict.findByMainKey("asdf").get().isEmpty());
	}

	@Test()
	public void test2() throws InterruptedException, ExecutionException {
		DoubleKeyDict testDict = f.create("test2");
		for (int i = 0; i < 3339; i++) {
			testDict.add(i + "", i + "", i + "");
		}
		//
		testDict.store();
		//
		assertEquals("1456", testDict.findByKeys("1456", "1456").get().get());
	}

	@Test()
	public void test3() throws InterruptedException, ExecutionException {
		DoubleKeyDict testDict = f.create("test3");
		testDict.add("b", "Niv", "c");
		testDict.add("a", "Dor", "c");
		testDict.store();
		//
		Map<String,String> res =  testDict.findByMainKey("b").get();
		assertEquals(1, res.size());
		assertEquals("c",res.get("Niv"));
	}

	@Test()
	public void test4() throws InterruptedException, ExecutionException {
		DoubleKeyDict testDict = f.create("test4");
		testDict.add("b", "Niv", "c");
		testDict.add("a", "Dor", "c");
		testDict.store();
		//
		Map<String,String> res =  testDict.findBySecondaryKey("Dor").get();
		assertEquals(1, res.size());
		assertEquals("c",res.get("a"));
	}

	@Test()
	public void test5() throws InterruptedException, ExecutionException {
		DoubleKeyDict testDict = f.create("test5");
		testDict.add("b", "Niv", "c");
		testDict.add("b", "Dor", "c");
		testDict.add("c", "x", "y");
		testDict.store();
		//
		Map<String,String> expected = new HashMap<>();
		expected.put("Dor", "c");
		expected.put("Niv", "c");
		Map<String,String> res = testDict.findByMainKey("b").get();
		//
		for (String k : expected.keySet())
			assertEquals(expected.get(k), res.get(k));
	}

	@Test()
	public void test6() throws InterruptedException, ExecutionException {
		DoubleKeyDict testDict = f.create("test6");
		testDict.add("b", "x", "c");
		testDict.add("b", "x", "c");
		testDict.add("c", "x", "y");
		testDict.store();
		//
		Map<String,String> expected = new HashMap<>();
		expected.put("b", "c");
		expected.put("b", "c");
		expected.put("c", "y");
		Map<String,String>  res = testDict.findBySecondaryKey("x").get();
		//
		for (String k : expected.keySet())
			assertEquals(expected.get(k), res.get(k));
	}
}
