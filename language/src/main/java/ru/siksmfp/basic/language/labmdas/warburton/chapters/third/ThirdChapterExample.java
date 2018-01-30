package ru.siksmfp.basic.language.labmdas.warburton.chapters.third;

import java.util.IntSummaryStatistics;
import java.util.stream.Stream;

/**
 * Created by Artyom Karnov on 26.11.16.
 * artyom-karnov@yandex.ru
 **/
public class ThirdChapterExample {
    public static void main(String[] args) {
        ThirdChapterExample thirdChapterExample = new ThirdChapterExample();
        thirdChapterExample.one();
    }

    public void one() {
        IntSummaryStatistics intSummaryStatistics = Stream.of(1, 2, 3)
                .mapToInt(Integer::intValue)
                .summaryStatistics();
        System.out.println(intSummaryStatistics.getAverage());
        System.out.println(intSummaryStatistics.getCount());
        System.out.println(intSummaryStatistics.getMax());
        System.out.println(intSummaryStatistics.getMin());
        System.out.println(intSummaryStatistics.getSum());
    }
}
