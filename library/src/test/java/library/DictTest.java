package library;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class DictTest {
	static Injector injector = Guice.createInjector(new TestLineStorageModule(), new LibraryModule());
	static DictFactory f = injector.getInstance(DictFactory.class);

	@Test()
	public void test0() throws InterruptedException, ExecutionException {
		Dict testDict = f.create("test0");
		testDict.add("a", "Dor");
		testDict.add("b", "Niv");
		testDict.store();
		//
		assertEquals("Niv", testDict.find("b").get().get());
	}

	@Test()
	public void test1() throws Exception {
		Dict testDict = f.create("test1");
		testDict.add("a", "Dor");
		testDict.store();
		//
		assertThat(testDict.find("asdf").get(), is(Optional.empty()));
	}

	@Test
	public void test2() throws Exception {
		Dict testDict = f.create("test2");
		for (int i = 0; i < 1000000; i++) {
			testDict.add(i + "", i + "");
		}
		testDict.store();
		//
		assertEquals(1456 + "", testDict.find("1456").get().get());
	}

	@Test()
	public void test3() throws Exception {
		Dict testDict = f.create("test3");
		testDict.add("b", "Niv");
		testDict.add("a", "Dor");
		testDict.store();
		//
		assertEquals("Niv", testDict.find("b").get().get());
	}
}
