package org.rx.tool;

public class Just {

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

    @FunctionalInterface
    public interface Proc {
        void go() throws Exception;
    }

    @FunctionalInterface
    public interface Fn<T> {
        T go() throws Exception;
    }
}
