package com.launchableinc.testng;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.kohsuke.MetaInfServices;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGListener;

import java.util.Iterator;
import java.util.List;

@MetaInfServices(ITestNGListener.class)
public class TestSelector implements IMethodInterceptor {

	private String LAUNCHABLE_SUBSET_FILE = "LAUNCHABLE_SUBSET_FILE_PATH";
	private String launchableDefaultSubsetFilePath = "subset.txt";

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext iTestContext) {

		String subsetFilePath =
				System.getenv(LAUNCHABLE_SUBSET_FILE) == null ? launchableDefaultSubsetFilePath
						: System.getenv(LAUNCHABLE_SUBSET_FILE);

		Map<String, String> subsetList = new HashMap<>();
		try (Scanner scanner = new Scanner(new FileReader(subsetFilePath))) {
			while (scanner.hasNext()) {
				String l = scanner.nextLine();
				subsetList.put(l, l);
			}
		} catch (FileNotFoundException e) {
			// TODO: use logger
			System.out.printf(
					"WARNING: Can not read file %s. Make sure to set subset result file path to env LAUNCHABLE_SUBST_FILE_PATH %n",
					launchableDefaultSubsetFilePath);
		}

		Iterator<IMethodInstance> itr = methods.iterator();
		while (itr.hasNext()) {
			IMethodInstance m = itr.next();
			if (subsetList.get(m.getMethod().getTestClass().getName()) == null) {
				itr.remove();
			}
		}

		return methods;
	}
}
