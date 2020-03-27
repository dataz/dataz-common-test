/*
 * dataZ - Test Support For Data Stores.
 *
 * Copyright 2014-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.failearly.dataz.common.test.junit4;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

/**
 * JUnit4TestUtilities runs a JUnit4 single test method of a test class.
 */
public class JUnit4TestUtilities {

    public static Result runTestMethod(Class<?> testClass, String testMethod) {
        final JUnitCore junit = new JUnitCore();
        final Request request = Request.method(testClass, testMethod);
        return junit.run(request);
    }
}
