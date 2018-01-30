package com.saiu.algorithms.interview.stepic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

/**
 * @author Artem Karnov @date 25.04.2017.
 *         artem.karnov@t-systems.com
 */

public class DeckSkipWithStream {
    public static Deque<Integer> skipLastElements(Deque<Integer> deque, int n) {
        Deque<Integer> queue = deque.stream().skip(2)
                .collect(Collectors.toCollection(ArrayDeque::new));
        return queue;
    }
}
