/*
 * dataZ - Test Support For Data Stores.
 *
 * Copyright (C) 2014-2016 'Marko Umek' (http://fail-early.com)
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

package org.failearly.common.test.threadsafety;

import org.failearly.common.test.ExceptionVerifier;
import org.failearly.common.test.annotations.TestsFor;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * ThreadSafetyVerifierTest contains tests for ThreadSafetyVerifier.
 */
@TestsFor({ThreadSafetyVerifier.class})
public class ThreadSafetyVerifierTest {

    private static final String PATTERN="yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String DATE_TO_PARSE0="2001-07-04T12:08:56.235-0700";
    private static final String DATE_TO_PARSE1="2016-03-18T12:08:56.235-0100";

    private void assertSimpleDateFormatter(SimpleDateFormat sharedDateFormatter, String dateAsString) throws ParseException {
        final Date expected=new SimpleDateFormat(PATTERN).parse(dateAsString);

        final Date actual=sharedDateFormatter.parse(dateAsString);
        assertEquals(expected, actual);
    }

    // QUESTION : What/How ... ?

    @Test
    public void not_thread_safety_class__using_VerificationBlockVerifier__should_be_detected() throws Throwable {
        final VerificationBlockVerifier<SimpleDateFormat> threadSafetyVerifier=ThreadSafetyVerifier
            .given(() -> new SimpleDateFormat(PATTERN))
            .whenAndThen(
                sharedDateFormatter -> assertSimpleDateFormatter(sharedDateFormatter, DATE_TO_PARSE0)
            )
            .or(
                sharedDateFormatter -> assertSimpleDateFormatter(sharedDateFormatter, DATE_TO_PARSE1)
            );

        ExceptionVerifier.on(threadSafetyVerifier::verify)
            .expect(AssertionError.class)
            .verify();
    }

    @Test
    public void not_thread_safety_class__using_ExecutionBlockVerifier__should_be_detected() throws Throwable {
        final SimpleDateFormat simpleDateFormat=new SimpleDateFormat(PATTERN);
        final Date expected0=simpleDateFormat.parse(DATE_TO_PARSE0);
        final Date expected1=simpleDateFormat.parse(DATE_TO_PARSE1);
        Predicate<Date> expect=(expected0::equals);
                        expect.or(expected1::equals);

        final ExecutionBlockVerifier<SimpleDateFormat,Date> threadSafetyVerifier=ThreadSafetyVerifier
            .given(Date.class, () -> new SimpleDateFormat(PATTERN))
            .when(sharedDateFormatter -> sharedDateFormatter.parse(DATE_TO_PARSE0))
            .or(sharedDateFormatter -> sharedDateFormatter.parse(DATE_TO_PARSE1))
            .then(expect)
            .thenAsserResulttList(ld->assertThat(new HashSet<>(ld), Matchers.containsInAnyOrder(expected0, expected1)));

        ExceptionVerifier.on(threadSafetyVerifier::verify)
            .expect(AssertionError.class)
            .verify();
    }

    // TODO: Test thread safe classes (VBV and EBV)
    // TODO: Test Unexpected Exception (VBV and EBV)
    // TODO: Test Assertion Error (VBV and EBV)
    // TODO: Test Timeout
    // TODO: Own unsafe class
    // TODO: Own safe class
    // TODO: Don't use random for access
    // TODO: help() on ThreadSafetyVerifier
}