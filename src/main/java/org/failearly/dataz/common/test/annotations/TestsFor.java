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

package org.failearly.dataz.common.test.annotations;

import java.lang.annotation.*;

/**
 * Adds information to the test class, which are the classes under test.
 *
 * Just for documentation purposes, not stored or available during runtime.
 *
 * @see Subject
 */
@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD,ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface TestsFor {
    Class<?>[] value() default {};

    Class<?>[] classes() default {};

    String[] methods() default {};
}
