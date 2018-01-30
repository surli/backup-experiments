package com.saiu.algorithms.interview.stepic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Artem Karnov @date 24.04.17.
 *         artem.karnov@t-systems.com
 */

public class StreamCollectionMax {
    public static Integer maxElem(List<Integer> list) {
        return list.stream().max(Integer::compareTo).get();
    }

    static List<String> changeList(List<String> list) {
        String max = new String("");
        for (String str : list) {
            if (str.length() >= max.length())
                max = str;
        }

        List<String> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            result.add(max);
        }

        return result;
    }

    static List<String> changeListCollectionLong(List<String> list) {
        Comparator<String> compare = (str1, str2) -> {
            return str1.length() < str2.length() ? -1 : 1;
        };

        return list.stream()
                .map(s -> s = list
                        .stream()
                        .max((s1, s2) -> {
                            return s1.length() < s2.length() ? -1 : 1;
                        })
                        .get())
                .collect(Collectors.toList());

    }

    static List<String> changeListCollectionShort(List<String> list) {
        return list.stream()
                .map(s -> s = list
                        .stream()
                        .max((s1, s2) -> {
                            return s1.length() < s2.length() ? -1 : 1;
                        })
                        .get())
                .collect(Collectors.toList());

    }

    static List<String> changeListOptimal(List<String> list) {
        return Collections
                .nCopies(list.size(), Collections.max(list, (x1, x2) -> x1.length() - x2.length()));
    }


}
