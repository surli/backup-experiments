package ru.siksmfp.basic.language.labmdas.warburton.chapters.first;

import java.util.function.BinaryOperator;

/**
 * Created by Artyom Karnov on 26.11.16.
 * artyom-karnov@yandex.ru
 **/


public class FirstChapterExamples {
    public static void main(String[] args) {
        Runnable noArguments = () -> System.out.println("Hi");
        noArguments.run();

        //Standard realization
        BinaryOperator<Integer> multiply = (x, y) -> x * y;
        BinaryOperator<Integer> division = (x, y) -> x / y;
        BinaryOperator<Integer> addition = (x, y) -> x + y;
        BinaryOperator<Integer> subtraction = (x, y) -> x - y;

        //Custom realization
        BinaryOperator<ClassWithData> XplusY = (X, Y) -> {
            X.sum(Y);
            return X;
        };

        System.out.println(XplusY.apply(new ClassWithData(10), new ClassWithData(10)).getData());

        operation(multiply, 5, 5); //25
        operation(division, 5, 5); //1
        operation(addition, 5, 5); //10
        operation(subtraction, 5, 5); //0

    }

    public static void operation(BinaryOperator<Integer> code, Integer a, Integer b) {
        int i = code.apply(a, b);
        System.out.println(i);
    }
}
