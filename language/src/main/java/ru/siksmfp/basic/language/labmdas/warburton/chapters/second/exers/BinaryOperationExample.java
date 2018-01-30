package ru.siksmfp.basic.language.labmdas.warburton.chapters.second.exers;

import java.util.stream.Stream;

/**
 * Created by Artyom Karnov on 27.11.16.
 * artyom-karnov@yandex.ru
 **/
public class BinaryOperationExample {
    public static void main(String[] args) {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 55);
        BinaryOperationExample oneA = new BinaryOperationExample();
        System.out.println(oneA.addUp(stream));

    }

    int addUp(Stream<Integer> numbers) {
        return numbers.reduce(0, (one, two) -> one + two);
    }
}

