package com.saiu.algorithms.interview.stepic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Artem Karnov @date 02.05.2017.
 *         artem.karnov@t-systems.com
 */

public class StringToSortedArrayStreams {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> list = Arrays.stream(reader.readLine().split("\\s")).collect(Collectors.toList());
        list
                .stream()
                .map(Integer::valueOf)
                .filter(s -> s % 2 == 1)
                .sorted()
                .forEach(System.out::println);

    }
}
