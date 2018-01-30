package ru.siksmfp.basic.interview.code.wars;

/**
 * @author Artem Karnov @date 07.03.17.
 * artem.karnov@t-systems.com
 */

/*

The word i18n is a common abbreviation of internationalization the developer
community use instead of typing the whole word and trying to spell it correctly.
Similarly, a11y is an abbreviation of accessibility.

Write a function that takes a string and turns any and all "words" (see below)
within that string of length 4 or greater into an abbreviation following the same rules.

Notes:

A "word" is a sequence of alphabetical characters. By this definition,
any other character like a space or hyphen (eg. "elephant-ride") will split up
a series of letters into two words (eg. "elephant" and "ride").
The abbreviated version of the word should have the first letter, then the number
of removed characters, then the last letter (eg. "elephant ride" => "e6t r2e").
Example:

abbreviate("elephant-rides are really fun!")
//          ^^^^^^^^*^^^^^*^^^*^^^^^^*^^^*
// words (^):   "elephant" "rides" "are" "really" "fun"
//                123456     123     1     1234     1
// ignore short words:               X              X

// abbreviate:    "e6t"     "r3s"  "are"  "r4y"   "fun"
// all non-word characters (*) remain in place
//                     "-"      " "    " "     " "     "!"
=== "e6t-r3s are r4y fun!"

 */

// TODO: 07.03.17 Finish it
public class Abbreviator {
    private String result = "";

    public static void main(String[] args) {
        Abbreviator obj = new Abbreviator();
        System.out.println(obj.abbreviate("elephant-rides are really fun!"));
        System.out.println(obj.abbreviate("internationalization"));
    }

    public String abbreviate(String string) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        int j = 0;
        while (i < string.length()) {
            int endingWordCorrectionCoefficient;
            if (!Character.isAlphabetic(string.charAt(i)) || i == string.length() - 1) {
                if (i - j < 4) {
                    result.append(string.substring(j, i));
                    result.append(string.charAt(i));
                } else {
                    result.append(string.charAt(j));
                    if (i == string.length() - 1) {
                        endingWordCorrectionCoefficient = 1;
                    } else {
                        endingWordCorrectionCoefficient = 2;
                    }
                    result.append(i - j - endingWordCorrectionCoefficient);
                    if (i != string.length() - 1)
                        result.append(string.charAt(i - 1));
                    result.append(string.charAt(i));
                }
                i++;
                j = i;
            } else {
                i++;
            }
        }
        return result.toString();
    }
}
