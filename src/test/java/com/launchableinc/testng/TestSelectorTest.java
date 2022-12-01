package com.launchableinc.testng;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import static org.testng.Assert.assertEquals;

public class TestSelectorTest {

	@Test
	public void not_intercept() {
		TestSelector selector = new TestSelector();
		TestNG tng =
				createTests("not-set-lists", Example1Test.class, Example2Test.class, Example3Test.class);
		tng.addListener(selector);
		tng.run();
		assertEquals(selector.totalTestCount, 9);
		assertEquals(selector.filteredCount, 0);
	}

	@Test
	public void intercept_set_subset_list() throws Exception {
		File file = File.createTempFile("subset-", ".txt");
		file.deleteOnExit();

		Files.write(file.toPath(),
				"com.launchableinc.testng.Example1Test".getBytes(StandardCharsets.UTF_8));


		TestSelector selector = new TestSelector(file, null);
		TestNG tng =
				createTests("set-subset-list", Example1Test.class, Example2Test.class, Example3Test.class);
		tng.addListener(selector);
		tng.run();
		assertEquals(selector.totalTestCount, 9);
		assertEquals(selector.filteredCount, 7);
	}

	@Test
	public void intercept_set_rest_list() throws Exception {
		File file = File.createTempFile("rest-", ".txt");
		file.deleteOnExit();

		Files.write(file.toPath(),
				"com.launchableinc.testng.Example1Test".getBytes(StandardCharsets.UTF_8));

		TestSelector selector = new TestSelector(null, file);
		TestNG tng =
				createTests("set-rest-list", Example1Test.class, Example2Test.class, Example3Test.class);
		tng.addListener(selector);
		tng.run();
		assertEquals(selector.totalTestCount, 9);
		assertEquals(selector.filteredCount, 2);

	}

	@Test
	public void intercept_set_multiple_subset_list() throws Exception {
		File file = File.createTempFile("subset-", ".txt");
		file.deleteOnExit();
		Files.write(file.toPath(),
				"com.launchableinc.testng.Example1Test\ncom.launchableinc.testng.Example2Test"
						.getBytes(StandardCharsets.UTF_8));

		TestSelector selector = new TestSelector(file, null);
		TestNG tng = createTests("set-subset-multiple-list", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();
		assertEquals(selector.totalTestCount, 9);
		assertEquals(selector.filteredCount, 3);

	}

	@Test
	public void intercept_set_multiple_rest_list() throws Exception {
		File file = File.createTempFile("rest-", ".txt");
		file.deleteOnExit();
		Files.write(file.toPath(),
				"com.launchableinc.testng.Example1Test\ncom.launchableinc.testng.Example2Test"
						.getBytes(StandardCharsets.UTF_8));

		TestSelector selector = new TestSelector(null, file);
		TestNG tng = createTests("set-rest-multiple-list", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();
		assertEquals(selector.totalTestCount, 9);
		assertEquals(selector.filteredCount, 6);

	}

	@Test
	public void intercept_set_subset_and_rest_list() throws Exception {
		File subset = File.createTempFile("subset-", ".txt");
		subset.deleteOnExit();
		Files.write(subset.toPath(),
				"com.launchableinc.testng.Example2Test".getBytes(StandardCharsets.UTF_8));

		File rest = File.createTempFile("rest-", ".txt");
		rest.deleteOnExit();
		Files.write(rest.toPath(),
				"com.launchableinc.testng.Example1Test\ncom.launchableinc.testng.Example3Test"
						.getBytes(StandardCharsets.UTF_8));

		TestSelector selector = new TestSelector(subset, rest);
		TestNG tng = createTests("set-subset-and-rest-list", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();
		assertEquals(selector.totalTestCount, 0);
		assertEquals(selector.filteredCount, 0);
	}

	@Test
	public void intercept_set_no_available_test_path() throws Exception {
		File file = File.createTempFile("subset-", ".txt");
		file.deleteOnExit();
		Files.write(file.toPath(),
				"com.launchableinc.testng.Sample1Test\ncom.launchableinc.testng.Sample2Test\ncom.launchableinc.testok.Example1Test"
						.getBytes(StandardCharsets.UTF_8));

		TestSelector selector = new TestSelector(file, null);
		TestNG tng =
				createTests("set-subset-list", Example1Test.class, Example2Test.class, Example3Test.class);
		tng.addListener(selector);
		tng.run();
		assertEquals(selector.totalTestCount, 9);
		assertEquals(selector.filteredCount, 9);
	}

	@Test
	public void intercept_set_not_exists_subset_file_path() throws Exception {
		TestSelector selector = new TestSelector(new File("invalid_subset_file.txt"), null);
		TestNG tng = createTests("invalid_subset_file.txt", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();
		// If subset file doesn't exist, selector will return the inputted methods as is.
		assertEquals(selector.totalTestCount, 0);
		assertEquals(selector.filteredCount, 0);
	}

	@Test
	public void intercept_set_empty_subset_file() throws Exception {
		File file = File.createTempFile("subset-", ".txt");
		file.deleteOnExit();
		Files.write(file.toPath(), "".getBytes(StandardCharsets.UTF_8));

		TestSelector selector = new TestSelector(file, null);
		TestNG tng = createTests("invalid_subset_file.txt", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();

		assertEquals(selector.totalTestCount, 9);
		assertEquals(selector.filteredCount, 9);
	}

	private TestNG createTests(String suiteName, Class<?>... testClasses) {
		XmlSuite suite = createXmlSuite(suiteName);
		for (Class<?> testClass : testClasses) {
			createXmlTest(suite, testClass.getName(), testClass);
		}

		TestNG result = new TestNG();
		result.setUseDefaultListeners(false);
		result.setVerbose(0);
		result.setXmlSuites(Collections.singletonList(suite));
		return result;
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