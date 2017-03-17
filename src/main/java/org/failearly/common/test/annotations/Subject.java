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

package org.failearly.common.test.annotations;

import java.lang.annotation.*;

/**
 * The subject(s) under test (SUT).
 *
 * Just for documentation purposes, not stored or available during runtime.
 *
 * @see TestsFor
 */
@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD,ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Subject {
    Class<?>[] value() default {};
}
