package com.launchableinc.testng;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestSelectorTest {


	@Test
	public void intercept_not_set_subset_list() {
		TestSelector selector = new TestSelector();

		TestNG tng = createTests("not-set-subset-list", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();
		Assert.assertEquals(selector.getTotalTestCount(), 9);
		Assert.assertEquals(selector.getFilteredCount(), 0);
	}

	@Test
	public void intercept_set_subset_list() throws IOException {
		File file = new File("subset.txt");
		Files.write(file.toPath(),
				"com.launchableinc.testng.Example2Test".getBytes(StandardCharsets.UTF_8));

		TestSelector selector = new TestSelector();

		TestNG tng = createTests("set-subset-list", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();
		Assert.assertEquals(selector.getTotalTestCount(), 9);
		Assert.assertEquals(selector.getFilteredCount(), 4);

		file.deleteOnExit();
	}

	@Test
	public void intercept_set_subset_list_with_env() throws IOException {
		File file =  File.createTempFile("subset-", ".txt");
		Files.write(file.toPath(),
				"com.launchableinc.testng.Example1Test".getBytes(StandardCharsets.UTF_8));
		setEnv(TestSelector.LAUNCHABLE_SUBSET_FILE, file.getPath());

		TestSelector selector = new TestSelector();

		TestNG tng = createTests("set-subset-list", Example1Test.class, Example2Test.class,
				Example3Test.class);
		tng.addListener(selector);
		tng.run();
		Assert.assertEquals(selector.getTotalTestCount(), 9);
		Assert.assertEquals(selector.getFilteredCount(), 2);

		file.deleteOnExit();
	}

	public static void setEnv(String key, String value) {
		try {
			Map<String, String> env = System.getenv();
			Class<?> cl = env.getClass();
			Field field = cl.getDeclaredField("m");
			field.setAccessible(true);
			Map<String, String> writableEnv = (Map<String, String>) field.get(env);
			writableEnv.put(key, value);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to set environment variable", e);
		}
	}


	private TestNG createTests(String suiteName, Class<?>... testClasses) {
		XmlSuite suite = createXmlSuite(suiteName);
		for (Class<?> testClass : testClasses) {
			createXmlTest(suite, testClass.getName(), testClass);
		}

		TestNG result = new TestNG();
		result.setUseDefaultListeners(false);
		result.setVerbose(1);
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