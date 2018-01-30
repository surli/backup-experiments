package ru.siksmfp.basic.interview.code.wars;

/**
 * @author Artem Karnov @date 10/27/2017.
 * artem.karnov@t-systems.com
 */

/*
 The input is a string str of digits. Cut the string into chunks
 (a chunk here is a substring of the initial string)
 of size sz (ignore the last chunk if its size is less than sz).

 If a chunk represents an integer such as the sum of the cubes of its
 digits is divisible by 2, reverse that chunk; otherwise rotate it to
 the left by one position. Put together these modified chunks and
 return the result as a string.

 If  sz is <= 0 or if str is empty return ""
 If sz is greater (>) than the length of str it is impossible to take a chunk of size sz hence return "".
 */
public class RevRot {
    private static final String EMPTY_STRING = "";

    public static String revRot(String strng, int sz) {
        StringBuilder result = new StringBuilder();
        if (sz <= 0 || strng.equals(EMPTY_STRING)) {
            return EMPTY_STRING;
        } else if (sz > strng.length()) {
            return EMPTY_STRING;
        } else {
            int begin = 0;
            while (begin < strng.length()) {
                String chunk = strng.substring(begin, begin + sz);
                begin += sz;
                if (isSumOfCubsEven(chunk)) {
                    result.append(reverseChunk(chunk));
                } else {
                    result.append(rotateChunk(chunk));
                }
            }
            return result.toString();
        }
    }

    private static String reverseChunk(String chunk) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = chunk.length() - 1; i >= 0; i--) {
            stringBuilder.append(chunk.charAt(i));
        }
        return stringBuilder.toString();
    }

    private static String rotateChunk(String chunk) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(chunk.charAt(chunk.length() - 1));
        for (int i = 1; i < chunk.length() - 2; i++) {
            stringBuilder.append(chunk.charAt(i));
        }
        stringBuilder.append(chunk.charAt(0));
        return stringBuilder.toString();
    }

    private static boolean isSumOfCubsEven(String chunk) {
        long sum = 0;
        for (char currentDigit : chunk.toCharArray()) {
            sum += Math.pow(Character.getNumericValue(currentDigit), 3);
        }
        return sum % 2 == 0;
    }
}
