package ru.siksmfp.basic.interview.code.wars;

/**
 * @author Artem Karnov @date 28.08.2017.
 * artem.karnov@t-systems.com
 */
//Complexity O(n), n - number of digits in input variable
public class ExpandForm {

    public static String calculate(long num) {
        StringBuffer result = new StringBuffer();
        char[] str = String.valueOf(num).toCharArray();
        int counter = 1;
        for (int i = str.length - 1; i >= 0; i--) {
            int integer = str[i] - 48;
            if (integer != 0) {
                result.insert(0, integer * counter).insert(0, " + ");
            }
            counter *= 10;
        }
        return result.substring(3).toString();
    }

}
