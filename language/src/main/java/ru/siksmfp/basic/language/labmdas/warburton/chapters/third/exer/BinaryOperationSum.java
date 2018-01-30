package ru.siksmfp.basic.language.labmdas.warburton.chapters.third.exer;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * @author Artem Karnov @date 31.01.2017.
 * artem.karnov@t-systems.com
 */
public class BinaryOperationSum {
    public static void main(String[] args) {
        BinaryOperationSum binaryOperationSum = new BinaryOperationSum();
        List<Integer> numbers = asList(1, 2, 3);
        System.out.println(binaryOperationSum.addUp(numbers.stream()));
    }

    public int addUp(Stream<Integer> numbers) {
        BinaryOperator<Integer> sum = (x, y) -> x + y;
        return numbers.reduce(sum).get();
    }
}
