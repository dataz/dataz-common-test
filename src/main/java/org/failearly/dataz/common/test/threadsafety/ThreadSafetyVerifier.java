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

package org.failearly.dataz.common.test.threadsafety;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.lang.Runtime.getRuntime;
import static org.junit.Assert.fail;


/**
 * @param <T> Supplier type or Type to test
 * @param <R> Result type
 * @param <X> ThreadSafetyVerifier extension type (derived type)
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "WeakerAccess", "unused", "OptionalGetWithoutIsPresent", "unchecked"})
public abstract class ThreadSafetyVerifier<T, R, X extends ThreadSafetyVerifier<T,R,X>> {

    static final Logger LOGGER=LoggerFactory.getLogger(ThreadSafetyVerifier.class);
    private static final Random randomNumberGenerator = new Random();

    private final Optional<Supplier<T>> supplier;

    private int repeat=100;
    private int numThreads=getRuntime().availableProcessors();

    ThreadSafetyVerifier(Supplier<T> supplier) {
        this.supplier=Optional.ofNullable(supplier);
    }

    /**
     * Create a verifier without result. So the verification must be done within the
     * {@link VerificationBlockVerifier.VerificationBlock}.
     *
     * @param supplier the initial object.
     * @param <T>      the class to test
     *
     * @return the verifier instance
     */
    public static <T> VerificationBlockVerifier<T> given(Supplier<T> supplier) {
        return new VerificationBlockVerifier<>(supplier);
    }

    /**
     * Create a verifier without return type.
     *
     * @param returnType the return type
     * @param supplier the initial object.
     * @param <T>      the class to test
     * @param <R>      the return type
     *
     * @return the verifier instance
     */
    public static <T, R> ExecutionBlockVerifier<T, R> given(Class<R> returnType, Supplier<T> supplier) {
        return new ExecutionBlockVerifier<>(supplier);
    }


    /**
     * Set the number of repetitions to force an error. The default is {@code 100}.
     *
     * @param repeat numberOfRepetions
     *
     * @return this
     */
    public final X repeat(int repeat) {
        if (repeat < 1) {
            throw new IllegalArgumentException("repeat >= 1");
        }

        this.repeat=repeat;
        return (X)this;
    }

    /**
     * Set the number of threads the shared instance (set by
     * {@link #given(Supplier) or {@link #given(Class, Supplier)}}) should be used. The default is the number of
     * available processors/cores.
     *
     * @param numThreads the number of threads
     *
     * @return this
     *
     * @see Runtime#availableProcessors()
     */
    public final X threads(int numThreads) {
        if (numThreads < 1) {
            throw new IllegalArgumentException("#Threads >= 1");
        }

        this.numThreads=numThreads;
        return (X)this;
    }

    /**
     * Calculate the number of threads by using the {@link Runtime#availableProcessors()} as input value.
     *
     * @param calcOperator the calculation operator.
     *
     * @return this
     */
    public final X calculate(UnaryOperator<Integer> calcOperator) {
        return threads(
            calcOperator.apply(getRuntime().availableProcessors())
        );
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public final void verify() throws Throwable {
        checkMandatoryFields();
        final ExecutorService executorService=Executors.newFixedThreadPool(
                            numThreads,
                            threadFactory(this.getClass())
                        );
        try {
            repeat(this.repeat, executorService, this::doVerify);
        } finally {
            shutdown(executorService);
        }
    }

    private static class TSVThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber=new AtomicInteger(1);
        private final ThreadGroup group = Thread.currentThread().getThreadGroup();
        private final AtomicInteger threadNumber=new AtomicInteger(1);
        private final String namePrefix;

        private TSVThreadFactory(Class<? extends ThreadSafetyVerifier> tsvClass) {
            namePrefix=tsvClass.getSimpleName()+"-" +
                poolNumber.getAndIncrement() +
                "-thread-";
        }

        public Thread newThread(Runnable runnable) {
            Thread thread=new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.MAX_PRIORITY);
            return thread;
        }
    }
    private static ThreadFactory threadFactory(Class<? extends ThreadSafetyVerifier> tsvClass) {
        return new TSVThreadFactory(tsvClass);
    }

    private static void shutdown(ExecutorService executorService) throws InterruptedException {
        executorService.shutdown();
        if (executorService.awaitTermination(10L, TimeUnit.SECONDS)) {
            LOGGER.info("Executor Service terminated!");
        } else {
            LOGGER.error("Executor Service TIMEOUT!");
        }
    }

    private void doVerify(ExecutorService es, int num) throws Throwable {
        LOGGER.info("Start iteration no {}", num);
        final CountDownLatch startedSignal=new CountDownLatch(numThreads);
        final CountDownLatch doneSignal=new CountDownLatch(numThreads);

        final List<Future<BlockResult<R>>> results=es.invokeAll(
            createCallableList(startedSignal, doneSignal)
        );

        LOGGER.info("Wait for done.");
        doneSignal.await();

        doVerifyForAssertionsAndUnexpectedExceptions(results);
        doVerifyResults(results);
    }

    static int nextRandomIdx(int size) {
        return randomNumberGenerator.nextInt(size);
    }

    abstract void doVerifyResults(List<Future<BlockResult<R>>> results);

    private void doVerifyForAssertionsAndUnexpectedExceptions(List<Future<BlockResult<R>>> results) throws Throwable {
        final List<BlockResult<R>> failures=results.stream()
            .filter(Future::isDone)
            .map(this::toBlockResult)
            .filter(BlockResult::isNotOk)
            .collect(Collectors.toList());

        if (!failures.isEmpty()) {
            final StringBuilder stringBuilder=new StringBuilder();
            stringBuilder
                .append("\n\n")
                .append("Caught ")
                .append(failures.size())
                .append(" assertion error(s) or unexpected exception(s)");

            final String failureMessage=failures.stream().reduce(
                stringBuilder,
                (sb, br) -> br.appendTo(sb),
                (sb1, sb2) -> new StringBuilder().append(sb1).append(sb2)
            ).toString();

            fail(failureMessage);
        }
    }

    final BlockResult<R> toBlockResult(Future<BlockResult<R>> f) {
        try {
            return f.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Callable<BlockResult<R>>> createCallableList(
        CountDownLatch startedSignal,
        CountDownLatch doneSignal) throws Throwable {
        final T initialValue=supplier.get().get();
        return doCreateCallableList(() -> doExecuteBlock(startedSignal, doneSignal, initialValue));
    }

    private List<Callable<BlockResult<R>>> doCreateCallableList(Callable<BlockResult<R>> callable) {
        final List<Callable<BlockResult<R>>> callableList=new ArrayList<>();
        for (int i=0; i < this.numThreads; i++) {
            callableList.add(callable);
        }
        return callableList;
    }

    abstract BlockResult<R> doExecuteBlock(
        CountDownLatch startedSignal,
        CountDownLatch doneSignal,
        T initialValue
    ) throws InterruptedException;


    void checkMandatoryFields() {
        if (!supplier.isPresent()) {
            throw new IllegalArgumentException("Missing supplier! Do not call with given() with null.");
        }
    }

    private interface Block {
        void apply(ExecutorService executorService, int num) throws Throwable;
    }

    private static void repeat(int numRepeats, ExecutorService es, Block block) throws Throwable {
        for (int i=0; i < numRepeats; i++) {
            block.apply(es, i);
        }
    }
}
