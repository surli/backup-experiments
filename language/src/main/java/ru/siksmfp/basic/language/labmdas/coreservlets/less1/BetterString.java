package ru.siksmfp.basic.language.labmdas.coreservlets.less1;


/**
 * @author Artem Karnov @date 17.02.2017.
 *         artem.karnov@t-systems.com
 */
@FunctionalInterface
interface BetterLambdas<T> {
    boolean compare(T str1, T str2);
}

public class BetterString {
    public static void main(String[] args) {
        BetterString betterString = new BetterString();
        String one = "one", two = "two", three = "three";
        BetterLambdas myLambdas = (str1, str2) -> {
            return str1.toString().length() > str2.toString().length() ? true : false;
        };

        System.out.println(betterString.whichBetter(one, two, myLambdas));

        System.out.println(betterString.whichBetter(three, two, (str1, str2) -> {
            return true;
        }));
    }

    public String whichBetter(String str1, String str2, BetterLambdas lambdas) {
        if (lambdas.compare(str1, str2))
            return str1;
        else
            return str2;
    }
}
