package com.demo.util;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamUtil {
    /**
     * Chains predicates by performing AND operation
     * @param predicates
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> chainPredicatesByAnd(Predicate<T>... predicates) {
        Predicate<T> p = Stream.of(predicates).reduce(x -> true, Predicate::and);
        return p;
    }
}
