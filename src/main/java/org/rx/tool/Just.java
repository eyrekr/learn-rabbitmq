package org.rx.tool;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Just {

    private static final Executor EXECUTOR_SERVICES = Executors.newFixedThreadPool(8);

    public static void run(final Proc... procs) {
        for (final Proc proc : procs)
            try {
                proc.go();
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
    }

    public static <T> T get(final Fn<T> fn) {
        try {
            return fn.go();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void sleep(final long millis) {
        run(() -> Thread.sleep(millis));
    }

    public static void async(final Proc... procs) {
        for (final var proc : procs) EXECUTOR_SERVICES.execute(() -> run(proc));
    }

    @FunctionalInterface
    public interface Proc {
        void go() throws Exception;
    }

    @FunctionalInterface
    public interface Fn<T> {
        T go() throws Exception;
    }
}
