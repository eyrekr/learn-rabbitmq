package org.rx.tool;


import java.security.SecureRandom;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;

public final class Seq<E> implements Iterable<E> {
    private static final Pattern NUMBERS = Pattern.compile("(-?\\d+)", Pattern.MULTILINE | Pattern.DOTALL);
    private static final SecureRandom RANDOM = Just.get(SecureRandom::getInstanceStrong);

    public final E value;
    public final E lastValue;
    public final int length;
    public final Seq<E> tail;
    public final boolean isEmpty;
    public final boolean isLast;

    private Seq(final E value, final Seq<E> tail) {
        this.value = value;
        this.isLast = (tail == null || tail.length == 0);
        this.lastValue = this.isLast ? value : tail.lastValue;
        this.length = (tail == null) ? 0 : tail.length + 1;
        this.tail = tail;
        this.isEmpty = (tail == null);
    }

    public static <T> Seq<T> empty() {
        return new Seq<>(null, null);
    }

    @SafeVarargs
    public static <T> Seq<T> of(final T first, final T... rest) {
        return rest != null ? fromArray(rest).addFirst(first) : new Seq<>(first, empty());
    }

    public static <T> Seq<T> fromArray(final T[] array) {
        return array == null ? empty() : range(0, array.length).carryMap(array, (arr, i) -> arr[i]);
    }

    public static <T> Seq<T> fromIterable(final Iterable<T> collection) {
        return fromIterator(collection.iterator());
    }

    public static <T> Seq<T> fromIterator(final Iterator<T> iterator) {
        return iterator.hasNext() ? new Seq<>(iterator.next(), fromIterator(iterator)) : empty();
    }

    public static Seq<Character> ofCharactersFromString(final String string) {
        return string == null ? empty() : range(0, string.length()).carryMap(string, String::charAt);
    }

    public static Seq<String> ofLinesFromString(final String string) {
        return fromArray(string.split("\n"));
    }

    public static Seq<Long> ofNumbersFromString(final String string) {
        final var matcher = NUMBERS.matcher(string);
        Seq<Long> seq = Seq.empty();
        while (matcher.find()) {
            seq = seq.addFirst(Long.parseLong(matcher.group()));
        }
        return seq.reverse();
    }

    public static Seq<Integer> range(final int startInclusive, final int endExclusive) {
        return startInclusive >= endExclusive ? empty() : new Seq<>(startInclusive, range(startInclusive + 1, endExclusive));
    }

    public Seq<E> addFirst(final E value) {
        return new Seq<>(value, this);
    }

    public Seq<E> addLast(final E value) {
        return isEmpty ? Seq.of(value) : new Seq<>(this.value, tail.addLast(value));
    }

    public Seq<E> addSeq(final Seq<E> seq) {
        return seq.reduceR(this, (acc, element) -> new Seq<>(element, acc));
    }

    public E at(final int i) {
        return (i % length == 0) ? value : i > 0 ? tail.at((i % length) - 1) : tail.at(i % length);
    }

    public E at(final long i) {
        return (i % length == 0) ? value : i > 0 ? tail.at((i % length) - 1L) : tail.at(i % length);
    }

    public E random() {
        return at(RANDOM.nextInt(length));
    }

    public boolean has(final E value) {
        return !isEmpty && (Objects.equals(this.value, value) || tail.has(value));
    }

    public int indexOfFirst(final E value) {
        if (isEmpty) return -1;
        if (Objects.equals(this.value, value)) return 0;
        final int index = tail.indexOfFirst(value);
        return index < 0 ? index : index + 1;
    }

    public boolean atLeastOneIs(final Predicate<? super E> predicate) {
        return !isEmpty && (predicate.test(value) || tail.atLeastOneIs(predicate));
    }

    public boolean atLeastOneIsNot(final Predicate<? super E> predicate) {
        return atLeastOneIs(predicate.negate());
    }

    public boolean allMatch(final E value) {
        return allAre(element -> Objects.equals(element, value));
    }

    public boolean allAre(final Predicate<? super E> predicate) {
        return isEmpty || predicate.test(this.value) && tail.allAre(predicate);
    }

    public boolean noneMatch(final E value) {
        return noneIs(element -> Objects.equals(element, value));
    }

    public boolean noneIs(final Predicate<? super E> predicate) {
        return isEmpty || !predicate.test(value) && tail.noneIs(predicate);
    }

    public <R> Seq<R> map(final Function<? super E, R> translate) {
        return isEmpty ? empty() : new Seq<>(translate.apply(value), tail.map(translate));
    }

    public <R> Seq<R> contextMap(final Function<Seq<E>, R> translate) {
        return isEmpty ? empty() : new Seq<>(translate.apply(this), tail.contextMap(translate));
    }

    public <C, R> Seq<R> carryMap(final C carry, final BiFunction<? super C, ? super E, R> translate) {
        return isEmpty ? empty() : new Seq<>(translate.apply(carry, value), tail.carryMap(carry, translate));
    }

    public <F, R> Seq<R> mapWith(final Seq<F> seq, final BiFunction<? super E, ? super F, R> translate) {
        return isEmpty || seq.isEmpty ? empty() : new Seq<>(translate.apply(value, seq.value), tail.mapWith(seq.tail, translate));
    }

    public <R> Seq<R> flatMap(final Function<? super E, Seq<R>> translate) { // FIXME It evaluates elements from back to front!
        return reduceR(empty(), (acc, element) -> acc.addSeq(translate.apply(element)));
    }

    public <R> Seq<R> mapWithNext(final BiFunction<? super E, ? super E, R> translate) {
        return isEmpty ? empty() : new Seq<>(translate.apply(value, tail.value), tail.mapWithNext(translate));
    }

    public <R> Seq<R> mapWithPrev(final BiFunction<? super E, ? super E, R> translate) {
        return mapWith(addFirst(null), translate);
    }

    public <R> R reduceR(final R init, final BiFunction<? super R, ? super E, ? extends R> combine) { // left-to-right
        return isEmpty ? init : combine.apply(tail.reduceR(init, combine), value);
    }

    public Opt<E> reduceR(final BiFunction<? super E, ? super E, ? extends E> combine) {
        return isEmpty ? Opt.empty() : Opt.of(tail.reduceR(value, combine));
    }

    public <R> R reduce(final R init, final BiFunction<? super R, ? super E, ? extends R> combine) { // right-to-left
        return isEmpty ? init : tail.reduce(combine.apply(init, value), combine);
    }

    public Opt<E> reduce(final BiFunction<? super E, ? super E, ? extends E> combine) {
        return isEmpty ? Opt.empty() : Opt.of(tail.reduce(value, combine));
    }

    public Seq<E> where(final Predicate<? super E> predicate) {
        return isEmpty ? this : predicate.test(value) ? new Seq<>(value, tail.where(predicate)) : tail.where(predicate);
    }

    public Opt<E> firstWhere(final Predicate<? super E> predicate) {
        return isEmpty ? Opt.empty() : predicate.test(value) ? Opt.of(value) : tail.firstWhere(predicate);
    }

    public Seq<E> reverse() {
        return reduce(empty(), (acc, element) -> new Seq<>(element, acc));
    }

    public Seq<E> sortedBy(final Comparator<? super E> comparator) {
        if (length <= 1) return this;
        if (length == 2) return comparator.compare(value, tail.value) <= 0 ? this : Seq.of(tail.value, value);

        final Seq<E> lower = tail.where(element -> comparator.compare(value, element) <= 0).sortedBy(comparator);
        final Seq<E> upper = tail.where(element -> comparator.compare(value, element) > 0).sortedBy(comparator);
        return lower.addFirst(value).addSeq(upper);
    }

    public <T extends Comparable<T>> Seq<E> sortedBy(final Function<? super E, T> property) {
        return sortedBy(Comparator.comparing(property::apply));
    }

    public Seq<E> sorted() {
        return sortedBy(Comparator.comparing((Object a) -> ((Comparable) a)));
    }

    public Seq<E> unique() {
        return unique(new HashSet<>());
    }

    public Seq<E> unique(final Set<E> visited) {
        if (isEmpty) return this;
        if (visited.contains(value)) return tail.unique(visited);
        visited.add(value);
        return new Seq<>(value, tail.unique(visited));
    }

    public Opt<E> min(final Comparator<? super E> comparator) {
        return reduce((a, b) -> comparator.compare(a, b) < 0 ? a : b);
    }

    public Opt<E> min() {
        return min(Comparator.comparing((Object a) -> ((Comparable) a)));
    }

    public Opt<E> max(final Comparator<? super E> comparator) {
        return reduce((a, b) -> comparator.compare(a, b) > 0 ? a : b);
    }

    public Opt<E> max() {
        return max(Comparator.comparing((Object a) -> ((Comparable) a)));
    }

    public Seq<Seq<E>> split(final Predicate<? super E> predicate) {
        return reduceR(
                Seq.of(empty(), empty()),
                (acc, element) -> predicate.test(element)
                        ? Seq.of(acc.value, acc.tail.value.addFirst(element))
                        : Seq.of(acc.value.addFirst(element), acc.tail.value));
    }

    public Seq<E> first(final int n) {
        return isEmpty ? this : n == 0 ? empty() : new Seq<>(value, tail.first(n - 1));
    }

    public Seq<E> last(final int n) {
        return length <= n ? this : tail.last(n);
    }

    public Seq<E> skip(final int n) {
        return isEmpty || n <= 0 ? this : tail.skip(n - 1);
    }

    public Seq<E> takeWhile(final Predicate<? super E> predicate) {
        return isEmpty ? this : predicate.test(value) ? new Seq<>(value, tail.takeWhile(predicate)) : empty();
    }

    public Seq<E> skipWhile(final Predicate<? super E> predicate) {
        return isEmpty ? this : predicate.test(value) ? tail.skipWhile(predicate) : this;
    }

    public Seq<E> each(final Consumer<? super E> consumer) {
        if (!isEmpty) {
            consumer.accept(value);
            tail.forEach(consumer);
        }
        return this;
    }

    public Seq<Seq<E>> batch(final int size) {
        record Accumulator<E>(Seq<Seq<E>> all, Seq<E> batch) {
        }
        final Accumulator<E> acc = reduceR(
                new Accumulator<>(empty(), empty()),
                (accumulator, element) -> accumulator.batch.length < size
                        ? new Accumulator<>(accumulator.all, accumulator.batch.addFirst(element))
                        : new Accumulator<>(accumulator.all.addFirst(accumulator.batch), Seq.of(element)));
        return acc.batch.isEmpty ? acc.all : acc.all.addFirst(acc.batch);
    }

    public <K, V> Map<K, V> toMap(final Function<? super E, K> key, final Function<? super E, V> value) {
        return reduce(
                new HashMap<>(length),
                (map, element) -> {
                    map.put(key.apply(element), value.apply(element));
                    return map;
                });
    }

    public <K> Map<K, E> indexBy(final Function<? super E, K> key) {
        return reduce(
                new HashMap<>(length),
                (map, element) -> {
                    map.put(key.apply(element), element);
                    return map;
                });
    }

    public <F, R> Seq<R> prodMap(final Seq<F> seq, final BiFunction<? super E, ? super F, R> transform) {
        return flatMap(left -> seq.map(right -> transform.apply(left, right)));
    }

    public Seq<E> replaceAt(final int i, final E replacement) {
        return isEmpty || i > length || i < 0 ? this : i == 0 ? new Seq<>(replacement, tail) : new Seq<>(value, tail.replaceAt(i - 1, replacement));
    }

    public Seq<E> replaceFirst(final E value, final E replacement) {
        return isEmpty ? this : Objects.equals(this.value, value) ? new Seq<>(replacement, tail) : new Seq<>(this.value, tail.replaceFirst(value, replacement));
    }

    public Seq<E> replaceAll(final E value, final E replacement) {
        return isEmpty ? this : Objects.equals(this.value, value)
                ? new Seq<>(replacement, tail.replaceAll(value, replacement))
                : new Seq<>(this.value, tail.replaceAll(value, replacement));
    }

    public Seq<E> removeAt(final int i) {
        return isEmpty || i > length || i < 0 ? this : i == 0 ? tail : new Seq<>(value, tail.removeAt(i - 1));
    }

    public Seq<E> removeFirst(final E value) {
        return isEmpty ? this : Objects.equals(this.value, value) ? tail : new Seq<>(this.value, tail.removeFirst(value));
    }

    public Seq<E> removeAll(final E value) {
        return isEmpty ? this : Objects.equals(this.value, value) ? tail.removeAll(value) : new Seq<>(this.value, tail.removeAll(value));
    }

    public Seq<E> print() {
        return print(" ");
    }

    public Seq<E> print(final String separator) {
        return print(separator, String::valueOf);
    }

    public Seq<E> print(final String separator, final Function<? super E, String> format) {
        final String string = "[" + reduceR("", (sum, element) -> sum.isBlank() ? format.apply(element) : format.apply(element) + separator + sum) + "]\n";
        System.out.print(string);
        return this;
    }

    @Override
    public String toString() {
        return "[" + reduceR("", (sum, element) -> sum.isBlank() ? element + "" : element + " " + sum) + "]";
    }

    public E[] toArray(final IntFunction<E[]> arrayGenerator) {
        final E[] array = arrayGenerator.apply(length);
        for (int i = 0; i < length; i++) array[i] = at(i);
        return array;
    }

    public Map<E, Integer> frequency() {
        return reduce(new HashMap<>(), (map, element) -> {
            final int frequency = map.getOrDefault(element, 0);
            map.put(element, frequency + 1);
            return map;
        });
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Seq that) {
            return that.isEmpty && this.isEmpty ||
                    (that.length == this.length
                            && Objects.equals(that.value, this.value)
                            && that.tail.equals(this.tail));
        }
        return false;
    }

    @Override
    public It iterator() {
        return new It(false);
    }

    public It loopingIterator() {
        return new It(true);
    }

    public final class It implements Iterator<E> {
        private Seq<E> seq = Seq.this;
        public final boolean loop;
        public long steps = 0;

        private It(final boolean loop) {
            this.loop = loop;
        }

        @Override
        public boolean hasNext() {
            return loop || !seq.isEmpty;
        }

        @Override
        public E next() {
            if (seq.isEmpty) {
                if (!loop) {
                    throw new IllegalStateException();
                }
                seq = Seq.this;
            }
            final E value = seq.value;
            seq = seq.tail;
            steps++;
            return value;
        }
    }
}