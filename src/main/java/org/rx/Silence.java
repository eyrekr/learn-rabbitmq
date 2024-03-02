package org.rx;

class Silence {

    static void silently(final Exceptional... exceptionals) {
        for (final Exceptional exceptional : exceptionals)
            try {
                exceptional.go();
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
    }

    @FunctionalInterface
    interface Exceptional {
        void go() throws Exception;
    }
}
