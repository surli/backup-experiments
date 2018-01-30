package ru.siksmfp.basic.language.labmdas.coreservlets.less1;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;


/**
 * @author Artem Karnov @date 31.01.2017.
 * artem.karnov@t-systems.com
 */

public class StringSort {

    public static void main(String[] args) {
        StringSort stringSort = new StringSort();
        List<String> strings = asList("a", "ab", "bbb", "c", "cccaabb", "ev", "ab", "eeb");
        System.out.println(stringSort.lengthSort(strings));
        System.out.println(stringSort.reverseSort(strings));
        System.out.println(stringSort.firstCharacterAlphabeticalSort(strings));
        System.out.println(stringSort.eSymbolsFirst(strings));
    }

    public List<String> lengthSort(List<String> strings) {
        Comparator<String> compare = (str1, str2) -> {
            return str1.length() >= str2.length() ? -1 : 1;
        };

        //this
        strings.stream().sorted((str1, str2) -> {
            return str1.length() >= str2.length() ? -1 : 1;
        }).collect(Collectors.toList());

        //or this
        return strings.stream().sorted(compare).collect(Collectors.toList());
    }

    public List<String> reverseSort(List<String> strings) {
        return strings.stream().sorted((str1, str2) ->
                str1.length() < str2.length() ? -1 : 1
        ).collect(Collectors.toList());
    }

    public List<String> firstCharacterAlphabeticalSort(List<String> strings) {
        return strings.stream().sorted((str1, str2) -> str1.charAt(0) < str2.charAt(0) ? -1 : 1).collect(Collectors.toList());
    }

    public List<String> eSymbolsFirst(List<String> strings) {
        return strings.stream().sorted((str1, str2) -> str1.charAt(0) == 'e' ? -1 : 1).collect(Collectors.toList());
    }

    public int methodForTest(String str1, String str2) {
        return str1.length() - str2.length();
    }

}

