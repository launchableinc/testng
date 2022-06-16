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

	private static String LAUNCHABLE_SUBSET_FILE = "LAUNCHABLE_SUBSET_FILE_PATH";
	private static String LAUNCHABLE_DEFAULT_SUBSET_FILE_PATH = "subset.txt";

	private static final Logger LOGGER = Logger.getLogger(TestSelector.class.getName());

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext iTestContext) {
		String subsetFilePath =
				System.getenv(LAUNCHABLE_SUBSET_FILE) == null ? LAUNCHABLE_DEFAULT_SUBSET_FILE_PATH
						: System.getenv(LAUNCHABLE_SUBSET_FILE);

		Set<String> subsetList = new HashSet<>();
		try (Scanner scanner = new Scanner(new FileReader(subsetFilePath))) {
			while (scanner.hasNext()) {
				String l = scanner.nextLine();
				subsetList.add(l);
			}
		} catch (FileNotFoundException e) {
			LOGGER.warning(String.format(
					"Can not read subset file %s. Make sure to set subset result file path to LAUNCHABLE_SUBST_FILE_PATH",
					LAUNCHABLE_DEFAULT_SUBSET_FILE_PATH));
		}

		Iterator<IMethodInstance> itr = methods.iterator();
		while (itr.hasNext()) {
			IMethodInstance m = itr.next();
			if (subsetList.contains(m.getMethod().getTestClass().getName())) {
				itr.remove();
			}
		}

		return methods;
	}
}
