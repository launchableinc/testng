package com.launchableinc.testng;


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

	/*package*/ int totalTestCount = 0;

	/*package*/ int filteredCount = 0;

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext iTestContext) {
		Set<String> subsetList = new HashSet<>();
		Set<String> restList = new HashSet<>();

		String subsetFile = System.getenv(LAUNCHABLE_SUBSET_FILE);
		String restFile = System.getenv(LAUNCHABLE_REST_FILE);

		if (subsetFile != null) {
			try {
				subsetList = readFromFile(subsetFile);
			} catch (FileNotFoundException e) {
				LOGGER.warning(String.format(
						"Can not read subset file %s. Make sure to set subset result file path to %s",
						subsetFile, LAUNCHABLE_SUBSET_FILE));
				return methods;
			}

			LOGGER.info(String.format("Subset file (%s) is loaded", subsetFile));
			if (subsetList.isEmpty()) {
				LOGGER.warning(String.format("Subset file %s is empty. Please check your configuration",
						subsetFile));
			}
		}

		if (restFile != null) {
			try {
				restList = readFromFile(restFile);
			} catch (FileNotFoundException e) {
				LOGGER.warning(String.format(
						"Can not read rest file %s. Make sure to set rest result file path to %s",
						restFile, LAUNCHABLE_REST_FILE));
				return methods;
			}

			LOGGER.info(String.format("Rest file (%s) is loaded", restFile));
			if (restList.isEmpty()) {
				LOGGER.warning(String.format("Rest file %s is empty. Please check your configuration",
						restFile));
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

	private Set<String> readFromFile(String filePath ) throws FileNotFoundException {
		Set<String> list = new HashSet<>();
		try (Scanner scanner = new Scanner(new FileReader(filePath))) {
			while (scanner.hasNext()) {
				String l = scanner.nextLine();
				list.add(l);
			}
		} catch (Exception e) {
			throw e;
		}

		return list;
	}
}
