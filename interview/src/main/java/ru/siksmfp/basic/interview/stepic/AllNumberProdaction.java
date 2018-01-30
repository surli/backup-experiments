package com.saiu.algorithms.interview.stepic;

import java.util.function.BinaryOperator;
import java.util.function.ToLongBiFunction;
import java.util.stream.LongStream;

/**
 * @author Artem Karnov @date 17.07.2017.
 *         artem.karnov@t-systems.com
 */

/*
* Write a lambda expression that accepts two long arguments
* as a range borders and calculates (returns) production of
* all numbers in this range (inclusively). It's guaranteed
* that 0 <= left border <= right border.
* if left border == right border then the result is any border.

Solution format. Submit your lambda expression in any valid format with ; on the end.

Examples: (x, y) -> x + y; (x, y) -> { return x + y; };

Sample Input 1:
0 1
Sample Output 1:
0
Sample Input 2:
2 2
Sample Output 2:
2
Sample Input 3:
1 4
Sample Output 3:
24
Sample Input 4:
5 15
Sample Output 4:
54486432000
*
* */
public class AllNumberProdaction {
    long result(final long l1, final long r1) {
        ToLongBiFunction re = (l, r) -> LongStream.rangeClosed(l1, r1).reduce(1L, (acc, x) -> acc * x);
        return re.applyAsLong(l1, r1);
    }


    BinaryOperator<Long> op = (x, y) -> {
        long res = 1;
        if (x == y)
            return x;
        while (x <= y) {
            res *= x;
            x++;
        }
        return res;
    };
}
