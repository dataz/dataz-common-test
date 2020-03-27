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

package org.failearly.dataz.common.test;

import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * ExceptionVerifier is verification builder, which verifies that the action (assigned by {@link #on(TestAction)}),
 * throws an exception.
 */
@SuppressWarnings("unused")
public class ExceptionVerifier {
    private static final Logger LOGGER=LoggerFactory.getLogger(ExceptionVerifier.class);

    private final TestAction action;
    private Class<? extends Throwable> expectedExceptionClass=Throwable.class;
    private String expectedMessage;
    private Class<? extends Throwable> expectedCauseClass;
    private String expectedCauseMessage;
    private Class<? extends Throwable> expectedRootCauseClass;
    private String expectedRootCauseMessage;
    private Matcher<String> expectedMessageMatcher;

    private ExceptionVerifier(TestAction action) {
        this.action=action;
    }

    /**
     * Creates an verifier fro given {@code action}, which must be verified by
     *
     * @param action the action that should cause an exception.
     *
     * @return new verifier.
     */
    public static ExceptionVerifier on(TestAction action) {
        return new ExceptionVerifier(action);
    }


    /**
     * Verify that the action has thrown an exception of class {@code exceptionClass}.
     *
     * @param exceptionClass the expected exception class.
     *
     * @return this
     */
    public ExceptionVerifier expect(Class<? extends Throwable> exceptionClass) {
        this.expectedExceptionClass=exceptionClass;
        return this;
    }

    /**
     * Verify that the action has thrown an exception with {@code message}.
     *
     * @param message the expected message.
     *
     * @return this
     */
    public ExceptionVerifier expect(String message) {
        this.expectedMessage=message;
        return this;
    }

    /**
     * Verify that the action has thrown an exception which matches the {@code expectedMessageMatcher}.
     *
     * @param expectedMessageMatcher the expected message (matcher).
     *
     * @return this
     */
    public ExceptionVerifier expect(Matcher<String> expectedMessageMatcher) {
        this.expectedMessageMatcher = expectedMessageMatcher;
        return this;
    }

    /**
     * Verify that the action has thrown an exception which has a cause of class {@code causeClass}.
     *
     * @param causeClass the expected exception class of the cause.
     *
     * @return this
     */
    public ExceptionVerifier expectCause(Class<? extends Throwable> causeClass) {
        this.expectedCauseClass=causeClass;
        return this;
    }

    /**
     * Verify that the action has thrown an exception with {@code causeMessage}.
     *
     * @param causeMessage the expected message of the cause.
     *
     * @return this
     *
     * @see #expectCause(Class)
     */
    public ExceptionVerifier expectCause(String causeMessage) {
        this.expectedCauseMessage=causeMessage;
        return this;
    }

    /**
     * Verify that the action has thrown an exception which has a cause of class {@code rootCauseClass}.
     *
     * @param rootCauseClass the expected exception class of the root cause.
     *
     * @return this
     */
    public ExceptionVerifier expectRootCause(Class<? extends Throwable> rootCauseClass) {
        this.expectedRootCauseClass=rootCauseClass;
        return this;
    }

    public ExceptionVerifier expectRootCause(String rootCauseMessage) {
        this.expectedRootCauseMessage=rootCauseMessage;
        return this;
    }

    /**
     * Does the verification.
     */
    public void verify() {
        boolean exception=false;
        try {
            action.run();
        } catch (Throwable ex) {
            LOGGER.info("Caught exception", ex);
            exception=true;
            assertException(ex);
            assertCause(ex);
            assertRootCause(ex);
        }
        if (!exception) {
            fail("No exception caught. Expected exception type: " + expectedExceptionClass.getSimpleName() + "!");
        }
    }

    private void assertCause(Throwable ex) {
        if (verifyCauseIsNecessary()) {
            final Throwable cause=ex.getCause();

            assertThat("Cause found?", cause, notNullValue());
            assertThat("(Cause) Exception type?", cause, instanceOf(expectedCauseClass()));
            if (null != this.expectedCauseMessage) {
                assertThat("(Cause) Exception message?", cause.getMessage(), is(expectedCauseMessage));
            }
        } else {
            LOGGER.debug("No cause verification necessary.");
        }
    }

    private void assertRootCause(Throwable ex) {
        if (verifyRootCauseIsNecessary()) {
            final Throwable cause=resolveRootCause(ex);

            assertThat("Root Cause found?", cause, notNullValue());
            assertThat("(Root Cause) Exception type?", cause, instanceOf(expectedRootCauseClass()));
            if (null != this.expectedRootCauseMessage) {
                assertThat("(Root Cause) Exception message?", cause.getMessage(), is(expectedRootCauseMessage));
            }
        } else {
            LOGGER.debug("No root cause verification necessary.");
        }
    }

    private Throwable resolveRootCause(Throwable ex) {
        Throwable rootCause=null;
        for (Throwable cause=ex.getCause(); cause != null; cause=cause.getCause()) {
            rootCause=cause;
        }
        return rootCause;
    }

    private Class<? extends Throwable> expectedCauseClass() {
        return this.expectedCauseClass != null ? this.expectedCauseClass : Throwable.class;
    }

    private Class<? extends Throwable> expectedRootCauseClass() {
        return this.expectedRootCauseClass != null ? this.expectedRootCauseClass : Throwable.class;
    }

    private boolean verifyCauseIsNecessary() {
        return null != this.expectedCauseClass || null != this.expectedCauseMessage;
    }

    private boolean verifyRootCauseIsNecessary() {
        return null != this.expectedRootCauseClass || null != this.expectedRootCauseMessage;
    }

    protected void assertException(Throwable ex) {
        assertThat("Exception type?", ex, instanceOf(expectedExceptionClass));
        if (expectedMessage != null) {
            assertThat("Exception message?", ex.getMessage(), is(expectedMessage));
        }
        if (expectedMessageMatcher != null) {
            assertThat("Exception message?", ex.getMessage(), expectedMessageMatcher);
        }
    }


    /**
     * TestAction used by {@link #on(TestAction)}.
     */
    @FunctionalInterface
    public interface TestAction {
        void run() throws Throwable;
    }
}
