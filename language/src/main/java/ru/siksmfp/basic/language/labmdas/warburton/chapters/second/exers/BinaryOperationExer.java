package ru.siksmfp.basic.language.labmdas.warburton.chapters.second.exers;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * @author Artem Karnov @date 13.03.2017.
 * artem.karnov@t-systems.com
 */

class BinaryOperationExer {
    public static void main(String[] args) {
        BinaryOperationExer exer = new BinaryOperationExer();
        List<Integer> numbers = asList(1, 2, 3);
        System.out.println(exer.addUp(numbers.stream()));
    }

    public int addUp(Stream<Integer> numbers) {
        BinaryOperator<Integer> sum = (x, y) -> x + y;
        return numbers.reduce(sum).get();
    }
}
