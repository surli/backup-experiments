package ru.siksmfp.basic.structure.utils.math;

/**
 * @author Artem Karnov @date 1/6/2018.
 * artem.karnov@t-systems.com
 */
public class Math {
    public static double pow(double base, double exponent) {
        // TODO: 1/6/2018  make your own realization
        return java.lang.Math.pow(base, exponent);
    }

    public static int getFirstSimpleNumberAfter(int after) {
        for (int i = after + 1; i < Integer.MAX_VALUE; i++) {
            if (isPrimeNumber(i)) {
                return i;
            }
        }
        throw new ArithmeticException("There is no prime numbers after " + after + " in [0, Integer.MAX_VALUE]");
    }

    public static boolean isPrimeNumber(int number) {
        if (number % 2 == 0) {
            return false;
        } else {
            for (int i = number - 2; i > 1; i -= 2) {
                if (number % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
