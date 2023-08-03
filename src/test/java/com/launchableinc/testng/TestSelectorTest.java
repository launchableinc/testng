package com.launchableinc.testng;


import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Enumeration;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class TestSelectorTest {
	private TestListenerAdapter testCounter;

	@Test
	public void not_intercept() {
		TestSelector selector = new TestSelector();
		runTests("not-set-lists", selector);
		assertTestCounts(9);
	}

	@Test
	public void intercept_set_subset_list() throws Exception {
		File file = createListFile("subset-", "com.launchableinc.testng.Example1Test");

		runTests("set-subset-list", new TestSelector(file, null));
		assertTestCounts(2);
	}

	@Test
	public void intercept_set_rest_list() throws Exception {
		File file = createListFile("rest-", "com.launchableinc.testng.Example1Test");

		runTests("set-rest-list", new TestSelector(null, file));
		assertTestCounts(7);

	}

	@Test
	public void intercept_set_multiple_subset_list() throws Exception {
		File file = createListFile("subset-", "com.launchableinc.testng.Example1Test\ncom.launchableinc.testng.Example2Test");

		runTests("set-subset-multiple-list", new TestSelector(file, null));
		assertTestCounts(6);
	}

	@Test
	public void intercept_set_multiple_rest_list() throws Exception {
		File file = createListFile("rest-", "com.launchableinc.testng.Example1Test\ncom.launchableinc.testng.Example2Test");

		runTests("set-rest-multiple-list", new TestSelector(null, file));
		assertTestCounts(3);
	}

	@Test
	public void intercept_set_subset_and_rest_list() throws Exception {
		File subset = createListFile("subset-", "com.launchableinc.testng.Example2Test");
		File rest = createListFile("rest-", "com.launchableinc.testng.Example1Test\ncom.launchableinc.testng.Example3Test");

		runTests("set-subset-and-rest-list", new TestSelector(subset, rest));
		// both are specified, so we refrain from doing subsetting at all.
		assertTestCounts(9);
	}

	@Test
	public void intercept_set_no_available_test_path() throws Exception {
		File file = createListFile("subset-", "com.launchableinc.testng.Sample1Test\ncom.launchableinc.testng.Sample2Test\ncom.launchableinc.testok.Example1Test");

		runTests("set-subset-list", new TestSelector(file, null));
		assertTestCounts(0);
	}

	@Test
	public void intercept_set_not_exists_subset_file_path() {
		runTests("invalid_subset_file.txt", new TestSelector(new File("invalid_subset_file.txt"), null));
		// If subset file doesn't exist, selector will return the inputted methods as is.
		assertTestCounts(9);
	}

	@Test
	public void intercept_set_empty_subset_file() throws Exception {
		File file = createListFile("subset-", "");

		runTests("invalid_subset_file.txt", new TestSelector(file, null));
		assertTestCounts(0);
	}



	@BeforeMethod
	public void setup() {
		testCounter = new TestListenerAdapter();
	}

	private void assertTestCounts(int total) {
		assertEquals(testCounter.getPassedTests().size(), total);
		assertEquals(testCounter.getFailedTests().size(), 0);

		// this might sound confusing, but methods filtered out by IMethodInterceptor do not count skipped
		// as far as TestNG is concerned. It's as if those tests were not even considered in the first place.
		assertEquals(testCounter.getSkippedTests().size(), 0);
	}

	private File createListFile(String prefix, String x) throws IOException {
		File file = File.createTempFile(prefix, ".txt");
		file.deleteOnExit();

		Files.write(file.toPath(), x.getBytes(StandardCharsets.UTF_8));
		return file;
	}


	private void runTests(String suiteName, TestSelector selector) {
		XmlSuite suite = createXmlSuite(suiteName);
		for (Class<?> testClass : asList(Example1Test.class, Example2Test.class,
			Example3Test.class)) {
			createXmlTest(suite, testClass.getName(), testClass);
		}

		TestNG result = new TestNG();
		result.setUseDefaultListeners(false);
		result.setVerbose(0);
		result.setXmlSuites(Collections.singletonList(suite));
		// this prevents TestNG from discovering TestSelector via service loader, to avoid double filtering
		// TestNG doesn't support multiple method interceptor (at least as of testing).
		result.setServiceLoaderClassLoader(new URLClassLoader(new URL[0]) {
			@Override
			public Enumeration<URL> getResources(String name) {
				return Collections.emptyEnumeration();
			}
		});
		result.addListener(testCounter);
		result.addListener(selector);
		result.run();
	}

	private XmlSuite createXmlSuite(String suiteName) {
		XmlSuite suite = new XmlSuite();
		suite.setName(suiteName);
		return suite;
	}

	private XmlTest createXmlTest(XmlSuite suite, String testClassName, Class<?>... testClasses) {
		XmlTest result = new XmlTest(suite);
		result.setName(testClassName);
		int index = 0;
		for (Class<?> c : testClasses) {
			XmlClass xc = new XmlClass(c.getName(), index++, true);
			result.getXmlClasses().add(xc);
		}

		return result;
	}
}
