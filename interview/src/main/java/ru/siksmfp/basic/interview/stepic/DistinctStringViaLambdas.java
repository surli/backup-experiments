package com.saiu.algorithms.interview.stepic;

/**
 * @author Artem Karnov @date 25.04.2017.
 * artem.karnov@t-systems.com
 */

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Write a lambda expression that accepts a list of strings and returns new list of distinct strings
 * <p>
 * (without repeating). The order of elements in the result list may be any
 * (elements will be sorted by the testing system).
 * <p>
 * If the input list doesn't contain any strings or contains only empty strings
 * then the result is an empty string ("").
 * <p>
 * Hints: it is possible to use sets, maps, lists and so on for helping.
 * <p>
 * You may write the lambda expression in any valid formats but with ; on the end.
 * <p>
 * Examples: x -> x; (x) -> { return x; };
 * <p>
 * Sample Input 1:
 * <p>
 * java scala java clojure clojure
 * <p>
 * Sample Output 1:
 * <p>
 * clojure java scala
 * <p>
 * Sample Input 2:
 * <p>
 * the three the three the three an an a
 * <p>
 * Sample Output 2:
 * <p>
 * a an the three
 */
public class DistinctStringViaLambdas {
    public static List<String> distinctString(List<String> list) {
        Function<List<String>, List<String>> function = x -> x
                .stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return function.apply(list);
    }


    public static List<String> distinctStringWithStream(List<String> list) {
        return list
                .stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
