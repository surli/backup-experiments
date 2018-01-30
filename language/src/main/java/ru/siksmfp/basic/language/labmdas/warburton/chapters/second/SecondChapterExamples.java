package ru.siksmfp.basic.language.labmdas.warburton.chapters.second;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * Created by Artyom Karnov on 26.11.16.
 * artyom-karnov@yandex.ru
 **/
public class SecondChapterExamples {
    List<Integer> list = new ArrayList<>();

    public SecondChapterExamples() {
        list.add(0);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
    }

    public static void main(String[] args) {
        SecondChapterExamples secondChapter = new SecondChapterExamples();
        secondChapter.funkOne();
        secondChapter.funkTwo();
        secondChapter.funkFour();
        secondChapter.funkFive();
    }

    public void funkOne() {
        long result = list.stream().filter(list -> list.intValue() == 1).count();
        System.out.println(result);
    }

    public void funkTwo() {
        List<String> toUpperCase = Stream.of("aaa", "bbb", "Ccc")
                .map(strs -> strs.toUpperCase())
                .collect(Collectors.toList());
        System.out.println(toUpperCase);
    }

    //// TODO: 27.11.16 Выражение, которое выводит слова без цифр!
    public void funkThree() {
        List<Integer> conCat = Stream.of(asList(1, 2), asList(3, 4)).
                flatMap(numbs -> numbs.stream()).
                collect(Collectors.toList());
    }

    public void funkFour() {
        int max = Stream.of(asList(1, 2, 3, 4, 5, 7, 1)).
                flatMap(integers -> integers.stream())
                .max(Comparator.comparing(Integer::intValue)).map(mx -> mx.intValue()).get();
        System.out.println(max);
    }

    public void funkFive() {
        int sum = Stream.of(1, 2, 3).reduce(10, (асс, element) -> асс + element);
        System.out.println(sum);
    }


    public boolean isDigit(String symbol) {
        try {
            Integer.parseInt(symbol);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
