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

package org.failearly.dataz.common.test.utils;

import java.lang.reflect.Method;

/**
 * ReflectionUtils is responsible for ...
 */
public class ReflectionUtils {
    /**
     * Resolve {@link Method} instance from class with given methodName.
     *
     * @param methodName the method's name
     * @param clazz      the class
     * @return the method instance
     * @throws NoSuchMethodException method has not been found
     */
    public static Method resolveMethodFromClass(String methodName, Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod(methodName);
    }
}
