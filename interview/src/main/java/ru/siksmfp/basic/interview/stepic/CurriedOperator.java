package com.saiu.algorithms.interview.stepic;

import java.util.function.Function;

/**
 * @author Artem Karnov @date 24.04.17.
 *         artem.karnov@t-systems.com
 */


interface CustomCurriedOperator {
    int operate(int x, int y, int z);
}

public class CurriedOperator {
      /*
      Write a curried form of the function f(x,y,z)=x+y∗y+z∗z∗zf(x,y,z)=x+y∗y+z∗z∗z
      using lambda expressions in Java 8 style. The result and x, y, z must be integer numbers.

      You may write the result in any valid formats but with ; on the end.

      An example: x -> y -> { };

      Sample Input 1:
      1 1 1
      Sample Output 1:
      3
      Sample Input 2:
      2 3 4
      Sample Output 2:
      75
     */

    public static int customIntegerCurried(int x, int y, int z) {
        CustomCurriedOperator myFunctionalInterface = (x1, y1, z1) -> {
            return x1 + y1 * y1 + z1 * z1 * z1;
        };
        return myFunctionalInterface.operate(x, y, z);
    }


    public static void integerCurried(final int x1, final int y1, final int z1) {
        Function<Integer, Function<Integer, Function<Integer, Integer>>> calculation
                = x -> y -> z -> x + y * y + z * z * z;
    }

    /*
    Write a curried function (using lambdas) that accepts
    four string arguments and concatenated all in one string following the rules:

    String cases: in the result string, first and second arguments must be in lower cases and third and fourth in upper cases.
    Order of arguments concatenation: first, third, second, fourth.
    You may write the result in any valid formats but with ; on the end.

    An example: x -> y -> { };

    Sample Input:
    aa bb cc dd
    Sample Output:
    aaCCbbDD
    */

    public static void stringCurried(final int x1, final int y1, final int z1) {
        Function<String, Function<String, Function<String, Function<String, String>>>> calculation
                = w -> x -> y -> z -> w.toLowerCase() + y.toUpperCase() + x.toLowerCase() + z.toUpperCase();
    }

}