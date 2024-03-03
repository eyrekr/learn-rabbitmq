package org.rx.tool;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Opt<E> {

    private static Opt<Object> EMPTY = new Opt<>(null);

    public final E value;
    public final boolean present;
    public final boolean missing;

    private Opt(final E value) {
        this.value = value;
        present = value != null;
        missing = value == null;
    }

    public static <T> Opt<T> of(final T value) {
        return value == null ? empty() : new Opt<>(value);
    }

    public static <T> Opt<T> empty() {
        return (Opt<T>) EMPTY;
    }

    public Opt<E> where(final Predicate<? super E> predicate) {
        if (missing) return this;
        return predicate.test(value) ? this : empty();
    }

    public <R> Opt<R> map(final Function<? super E, ? extends R> transform) {
        return missing ? empty() : new Opt<>(transform.apply(value));
    }

    public <R> Opt<R> flatMap(final Function<? super E, Opt<? extends R>> transform) {
        return missing ? empty() : (Opt<R>) transform.apply(value);
    }

    public Opt<E> ifPresent(final Consumer<? super E> consumer) {
        if (present) consumer.accept(value);
        return this;
    }

    public E value() {
        return value;
    }

    public boolean isPresent() {
        return present;
    }

    public boolean isMissing() {
        return missing;
    }

    public E get() {
        if (missing) throw new IllegalStateException();
        return value;
    }

    public E getOrElse(final E defaultValue) {
        return present ? value : defaultValue;
    }

    public E getOrElse(final Supplier<? extends E> supplier) {
        return present ? value : supplier.get();
    }

    public E getOrThrow(final Supplier<? extends RuntimeException> supplier) {
        if (missing) throw supplier.get();
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final Opt that && this.present == that.present && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
