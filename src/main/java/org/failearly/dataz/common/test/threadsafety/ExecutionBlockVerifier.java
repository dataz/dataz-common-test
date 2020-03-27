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

package org.failearly.dataz.common.test.threadsafety;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.failearly.dataz.common.test.threadsafety.BlockResult.caughtUnexpectedException;
import static org.failearly.dataz.common.test.threadsafety.BlockResult.ok;
import static org.junit.Assert.fail;

/**
 * Description see {@link ThreadSafetyVerifier#given(Class, Supplier)}.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "WeakerAccess"})
public final class ExecutionBlockVerifier<T, R> extends ThreadSafetyVerifier<T, R, ExecutionBlockVerifier<T, R>> {
    private List<ExecutionBlock<T, R>> executionBlocks=new ArrayList<>();
    private Optional<Predicate<R>> verifier=Optional.empty();
    private Optional<Consumer<List<R>>> assertResultsList =Optional.empty();
    private Optional<Consumer<Set<R>>> assertResultsSet =Optional.empty();


    ExecutionBlockVerifier(Supplier<T> supplier) {
        super(supplier);
    }

    /**
     * Used by {@link #when(ExecutionBlock)}.
     */
    @FunctionalInterface
    public interface ExecutionBlock<T, R> {
        R apply(T sharedVar) throws Throwable;
    }

    /**
     * Add a (mandatory) execution block.
     *
     * @param executionBlock the execution block (returning R)
     *
     * @return this
     */
    public ExecutionBlockVerifier<T, R> when(ExecutionBlock<T, R> executionBlock) {
        this.executionBlocks.add(executionBlock);
        return this;
    }

    /**
     * Alias for {@link #when(ExecutionBlock)}.
     *
     * @param executionBlock the execution block (returning R)
     *
     * @return this
     */
    public ExecutionBlockVerifier<T, R> or(ExecutionBlock<T, R> executionBlock) {
        return when(executionBlock);
    }

    /**
     * The predicate will be used to check each item for correctness. If one return false, the (shared) object is not
     * thread safe.
     *
     * @param predicate the predicate, verifying the result of the execution block {@link #when(ExecutionBlock)}.
     *
     * @return this
     */
    public ExecutionBlockVerifier<T, R> then(Predicate<R> predicate) {
        if (!this.verifier.isPresent()) {
            this.verifier=Optional.of(predicate);
        } else {
            this.verifier=Optional.of(this.verifier.get().or(predicate));
        }
        return this;
    }

    /**
     * Assert the entire result list. You can use every assertion function (i.e. {@link org.junit.Assert})
     *
     * @param assertResultList the assert function.
     *
     * @return this
     */
    public ExecutionBlockVerifier<T, R> thenAsserResulttList(Consumer<List<R>> assertResultList) {
        this.assertResultsList =Optional.ofNullable(assertResultList);
        return this;
    }

    /**
     * Assert the entire result list. You can use every assertion function (i.e. {@link org.junit.Assert})
     *
     * @param assertResultSet the assert function.
     *
     * @return this
     */
    public ExecutionBlockVerifier<T, R> thenAssertResultSet(Consumer<Set<R>> assertResultSet) {
        this.assertResultsSet =Optional.ofNullable(assertResultSet);
        return this;
    }

    /**
     * Convert the result list to a result set.
     * @param results the result List
     * @param <XR> any type
     * @return a set
     */
    protected static <XR> Set<XR> toSet(List<XR> results) {
        return new HashSet<>(results);
    }


    @Override
    void checkMandatoryFields() {
        super.checkMandatoryFields();
        if (executionBlocks.isEmpty()) {
            throw new IllegalArgumentException("Missing execution block! Please use when() with an appropriate " +
                "execution block.");
        }
        if (!(verifier.isPresent() || assertResultsList.isPresent() || assertResultsSet.isPresent())) {
            throw new IllegalArgumentException("Missing verifier, assertResultSet or assertResultsList!");
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected BlockResult<R> doExecuteBlock(
        CountDownLatch startedSignal,
        CountDownLatch doneSignal,
        T initialValue
    ) throws InterruptedException {
        final int idx=nextRandomIdx(executionBlocks.size());
        startedSignal.countDown();
        LOGGER.debug("Thread {} has been started.", Thread.currentThread().getName());
        startedSignal.await();
        try {
            return ok(executionBlocks.get(idx).apply(initialValue));
        } catch (Throwable ex) {
            LOGGER.debug("Unexpected exception caught.", ex);
            return caughtUnexpectedException(ex);
        } finally {
            doneSignal.countDown();
            LOGGER.debug("Thread {} has been done.", Thread.currentThread().getName());
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void doVerifyResults(List<Future<BlockResult<R>>> results) {
        final List<R> allResults=results.stream()
            .map(this::toBlockResult)
            .map(BlockResult::getValue)
            .filter(Optional::isPresent)
            .flatMap(ov -> Stream.of(ov.get()))
            .collect(Collectors.toList());
        if (this.verifier.isPresent()) {
            final Predicate<R> notVerifier=this.verifier.get().negate();
            allResults.stream()
                .filter(notVerifier)
                .findAny()
                .ifPresent((ov) -> fail("At least one verifier failed"));
        }
        this.assertResultsList.ifPresent(va -> va.accept(allResults));
        this.assertResultsSet.ifPresent(va -> va.accept(toSet(allResults)));

    }
}
