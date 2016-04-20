/*
 * dataZ - Test Support For Data Stores.
 *
 * Copyright (C) 2014-2016 marko (http://fail-early.com/contact)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.failearly.common.test.annotations;

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
