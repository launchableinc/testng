package com.launchableinc.testng;

import org.kohsuke.MetaInfServices;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGListener;

import java.util.Iterator;
import java.util.List;

@MetaInfServices(ITestNGListener.class)
public class TestSelector implements IMethodInterceptor {
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext iTestContext) {
        System.out.printf("Test selector invoked with %d methods%n", methods.size());
        Iterator<IMethodInstance> itr = methods.iterator();
        while (itr.hasNext()) {
            IMethodInstance m =  itr.next();
            if (m.getMethod().getTestClass().getName().endsWith(".Test2"))
                itr.remove();
        }
        return methods;
    }
}
