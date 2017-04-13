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

package org.failearly.dataz.internal.common.test.threadsafety;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.failearly.dataz.internal.common.test.threadsafety.BlockResult.*;

/**
 * Description see {@link ThreadSafetyVerifier#given(Supplier)}.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "WeakerAccess"})
public final class VerificationBlockVerifier<T> extends ThreadSafetyVerifier<T, Void, VerificationBlockVerifier<T>> {
    private List<VerificationBlock<T>> verificationBlocks=new ArrayList<>();

    VerificationBlockVerifier(Supplier<T> supplier) {
        super(supplier);
    }

    @FunctionalInterface
    public interface VerificationBlock<T> {
        @SuppressWarnings("DuplicateThrows")
        void apply(T sharedVar) throws AssertionError, Throwable;
    }

    /**
     * Adds a verification block. (Multiple
     * @param verificationBlock a verification block (including {@link org.junit.Assert#assertTrue(boolean)} or something
     *                          similar.
     * @return this
     */
    public VerificationBlockVerifier<T> whenAndThen(VerificationBlock<T> verificationBlock) {
        if( verificationBlock==null ) {
            throw new IllegalArgumentException("Verification block must not be null.");
        }
        this.verificationBlocks.add(verificationBlock);
        return this;
    }

    /**
     * Alias for {@link #whenAndThen(VerificationBlock)}.
     * @param verificationBlock another verification block
     * @return this
     */
    public VerificationBlockVerifier<T> or(VerificationBlock<T> verificationBlock) {
        return whenAndThen(verificationBlock);
    }

    @Override
    void checkMandatoryFields() {
        super.checkMandatoryFields();
        if (verificationBlocks.isEmpty()) {
            throw new IllegalArgumentException("Missing verification blocks! Please apply at whenAndThen() with an " +
                "appropriate execution block.");
        }

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected BlockResult<Void> doExecuteBlock(
        CountDownLatch startedSignal,
        CountDownLatch doneSignal,
        T initialValue
    ) throws InterruptedException {
        final int idx=nextRandomIdx(verificationBlocks.size());
        startedSignal.countDown();
        LOGGER.debug("Thread {} has been started.", Thread.currentThread().getName());
        startedSignal.await();
        try {
            verificationBlocks.get(idx).apply(initialValue);
            return ok();
        } catch (AssertionError ae) {
            LOGGER.debug("Assertion error caught.", ae);
            return caughtAssertion(ae);
        } catch (Throwable ex) {
            LOGGER.debug("Unexpected exception caught.", ex);
            return caughtUnexpectedException(ex);
        } finally {
            doneSignal.countDown();
            LOGGER.debug("Thread {} has been done.", Thread.currentThread().getName());
        }
    }

    @Override
    protected void doVerifyResults(List<Future<BlockResult<Void>>> results) {
        // do nothing.
    }
}
