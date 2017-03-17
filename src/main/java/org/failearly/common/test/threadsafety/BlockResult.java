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

package org.failearly.common.test.threadsafety;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

/**
 * BlockResult is a (internal) class for {@link ThreadSafetyVerifier}. It collects the result of
 * the execution of {@link ExecutionBlockVerifier.ExecutionBlock} or {@link VerificationBlockVerifier.VerificationBlock}.
 * <br><br>
 * <ul>
 *    <li>An ({@link AssertionError})</li>
 *    <li>An unexpected unexpectedException {@link Throwable}</li>
 *    <li>or the result object of type R</li>
 * </ul>
 * @param <R> any result type (incl. Void)
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
final class BlockResult<R> {
    private static final BlockResult<Void> OK=new BlockResult<>(null, null, null);

    private final String threadName = Thread.currentThread().getName();
    private final Optional<AssertionError> assertionError;
    private final Optional<Throwable> unexpectedException;
    private final Optional<R> value;

    private BlockResult(AssertionError assertionError, Throwable unexpectedException, R value) {
        this.assertionError=Optional.ofNullable(assertionError);
        this.unexpectedException=Optional.ofNullable(unexpectedException);
        this.value=Optional.ofNullable(value);
    }

    static BlockResult<Void> ok() {
        return OK;
    }

    static <R> BlockResult<R> ok(R value) {
        return new BlockResult<>(null, null, value);
    }

    static <R> BlockResult<R> caughtAssertion(AssertionError assertionError) {
        return new BlockResult<>(assertionError, null, null);
    }

    static <R> BlockResult<R> caughtUnexpectedException(Throwable exception) {
        return new BlockResult<>(null, exception, null);
    }

    boolean isNotOk() {
        return assertionError.isPresent() || unexpectedException.isPresent();
    }

    Optional<R> getValue() {
        return value;
    }

    StringBuilder appendTo(StringBuilder stringBuilder) {
        assert isNotOk() : "Only in case of any exception";
        stringBuilder.append("\n\n\n");
        if (assertionError.isPresent()) {
            stringBuilder.append("AssertionError caught in thread ").append(threadName).append(":\n\n");
            stringBuilder.append(printToString(assertionError.get()));
        }
        if (unexpectedException.isPresent()) {
            stringBuilder.append("Unexpected exception caught in thread ").append(threadName).append(":\n\n");
            stringBuilder.append(printToString(unexpectedException.get()));
        }
        return stringBuilder;
    }


    private static String printToString(Throwable ex) {
        final ByteArrayOutputStream boas=new ByteArrayOutputStream();
        ex.printStackTrace(new PrintStream(boas));
        return boas.toString();
    }
}
