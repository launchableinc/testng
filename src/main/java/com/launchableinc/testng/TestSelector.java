package com.launchableinc.testng;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;
import org.kohsuke.MetaInfServices;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGListener;

import java.util.Iterator;
import java.util.List;

@MetaInfServices(ITestNGListener.class)
public class TestSelector implements IMethodInterceptor {

	public static final String LAUNCHABLE_SUBSET_FILE = "LAUNCHABLE_SUBSET_FILE_PATH";

	public static final String LAUNCHABLE_REST_FILE = "LAUNCHABLE_REST_FILE_PATH";

	private static final Logger LOGGER = Logger.getLogger(TestSelector.class.getName());

	private final File subsetFile;

	private final File restFile;

	/* package */ int totalTestCount = 0;

	/* package */ int filteredCount = 0;

	/* package */ public TestSelector(File subsetFile, File restFile) {
		this.subsetFile = subsetFile;
		this.restFile = restFile;
	}

	public TestSelector() {
		this.subsetFile = System.getenv(LAUNCHABLE_SUBSET_FILE) == null ? null
				: new File(System.getenv(LAUNCHABLE_SUBSET_FILE));

		this.restFile = System.getenv(LAUNCHABLE_REST_FILE) == null ? null
				: new File(System.getenv(LAUNCHABLE_REST_FILE));
	}

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext iTestContext) {
		Set<String> subsetList = new HashSet<>();
		Set<String> restList = new HashSet<>();

		if (this.subsetFile != null && this.restFile != null) {
			LOGGER.warning(String.format(
					"ERROR: Cannot set subset file (%s) and rest file (%s) both. Make sure set only one side.",
					subsetFile, restFile));
			return methods;
		}

		if (subsetFile != null) {
			try {
				subsetList = readFromFile(subsetFile);
			} catch (FileNotFoundException e) {
				LOGGER.warning(String.format(
						"Cannot read subset file %s. Make sure to set subset result file path to %s",
						subsetFile, LAUNCHABLE_SUBSET_FILE));
				return methods;
			}

			LOGGER.info(String.format("Subset file (%s) is loaded", subsetFile));
			if (subsetList.isEmpty()) {
				LOGGER.warning(
						String.format("Subset file %s is empty. Please check your configuration", subsetFile));
			}
		} else if (restFile != null) {
			try {
				restList = readFromFile(restFile);
			} catch (FileNotFoundException e) {
				LOGGER.warning(
						String.format("Cannot read rest file %s. Make sure to set rest result file path to %s",
								restFile, LAUNCHABLE_REST_FILE));
				return methods;
			}

			LOGGER.info(String.format("Rest file (%s) is loaded", restFile));
			if (restList.isEmpty()) {
				LOGGER.warning(
						String.format("Rest file %s is empty. Please check your configuration", restFile));
			}
		}

		Iterator<IMethodInstance> itr = methods.iterator();
		while (itr.hasNext()) {
			IMethodInstance m = itr.next();
			String className = m.getMethod().getTestClass().getName();
			totalTestCount++;

			if (subsetFile != null && !subsetList.contains(className)) {
				itr.remove();
				filteredCount++;
				continue;
			}

			if (restFile != null && restList.contains(className)) {
				itr.remove();
				filteredCount++;
			}
		}

		return methods;
	}

	private Set<String> readFromFile(File file) throws FileNotFoundException {
		Set<String> list = new HashSet<>();
		try (Scanner scanner = new Scanner(new FileReader(file))) {
			while (scanner.hasNext()) {
				String l = scanner.nextLine();
				list.add(l);
			}
		}

		return list;
	}
}
