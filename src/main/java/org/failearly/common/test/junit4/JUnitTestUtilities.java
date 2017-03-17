/*
 * dataZ - Test Support For Data Stores.
 *
 * Copyright 2014-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.failearly.common.test.junit4;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

/**
 * org.failearly.common.test.junit4.JUnitTestUtilities is responsible for ...
 */
public class JUnitTestUtilities {

    public static Result runTestMethod(Class<?> testClass, String testMethod) {
        final JUnitCore junit = new JUnitCore();
        final Request request = Request.method(testClass, testMethod);
        return junit.run(request);
    }
}
