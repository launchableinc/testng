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

	private static final Logger LOGGER = Logger.getLogger(TestSelector.class.getName());

	/*package*/  int totalTestCount = 0;

	/*package*/  int filteredCount = 0;

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext iTestContext) {
		Set<String> subsetList = new HashSet<>();

		if (System.getenv(LAUNCHABLE_SUBSET_FILE) != null) {
			String file = System.getenv(LAUNCHABLE_SUBSET_FILE);
			try (Scanner scanner = new Scanner(new FileReader(file))) {
				while (scanner.hasNext()) {

					String l = scanner.nextLine();
					subsetList.add(l);
				}
			} catch (FileNotFoundException e) {
				LOGGER.warning(String.format(
						"Can not read subset file %s. Make sure to set subset result file path to %s",
						file, LAUNCHABLE_SUBSET_FILE));
			}

			if (subsetList.isEmpty()) {
				LOGGER.warning(String.format("Subset file %s is empty. Please check your configuration",
						file));
			}
		}

		Iterator<IMethodInstance> itr = methods.iterator();
		while (itr.hasNext()) {
			IMethodInstance m = itr.next();
			totalTestCount++;
			if (subsetList.contains(m.getMethod().getTestClass().getName())) {
				itr.remove();
				filteredCount++;
			}
		}

		return methods;
	}
}
