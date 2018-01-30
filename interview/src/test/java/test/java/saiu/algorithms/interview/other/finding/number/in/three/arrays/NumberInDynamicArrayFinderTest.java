package test.java.saiu.algorithms.interview.other.finding.number.in.three.arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ru.siksmfp.basic.interview.other.NumberInArrayFinder;
import ru.siksmfp.basic.structure.array.fixed.FixedArray;

/**
 * @author Artem Karnov @date 25.02.17.
 * artem.karnov@t-systems.com
 */

public class NumberInDynamicArrayFinderTest {
    private NumberInArrayFinder<Integer> finder;
    private FixedArray<Integer> first;
    private FixedArray<Integer> second;
    private FixedArray<Integer> third;

    @Before
    public void setUp() {
        finder = new NumberInArrayFinder<>();
        first = null;
        second = null;
        third = null;
    }

    // 6 7 8 8 9 10 11 12 13 14 15
    // 1 2 5 6 7
    // -1 2 3 4 5  7
    // 6
    public void getFirstArraysSet() {
        first = new FixedArray<>(10);
        second = new FixedArray<>(5);
        third = new FixedArray<>(7);

        first.add(0, 6);
        first.add(1, 7);
        first.add(2, 8);
        first.add(3, 8);
        first.add(4, 9);
        first.add(5, 10);
        first.add(6, 11);
        first.add(7, 12);
        first.add(8, 13);
        first.add(9, 15);

        second.add(0, 1);
        second.add(1, 2);
        second.add(2, 5);
        second.add(3, 6);
        second.add(4, 7);

        third.add(0, -1);
        third.add(1, 2);
        third.add(2, 3);
        third.add(3, 4);
        third.add(4, 5);
        third.add(5, 6);
        third.add(6, 7);

    }

    // 5 10 15 20 25
    // 1 25 50 75 100
    // 1 2 3 4 25
    // 25
    public void getSecondArraysSet() {
        first = new FixedArray<>(5);
        second = new FixedArray<>(5);
        third = new FixedArray<>(5);

        first.add(0, 5);
        first.add(1, 10);
        first.add(2, 15);
        first.add(3, 20);
        first.add(4, 25);

        second.add(0, 1);
        second.add(1, 25);
        second.add(2, 50);
        second.add(3, 75);
        second.add(4, 100);

        third.add(0, 1);
        third.add(1, 2);
        third.add(2, 3);
        third.add(3, 4);
        third.add(4, 25);


    }

    // 5 5 5 6 7
    // 5 5 5 6 7
    // 8 8 8 9 9
    // 0
    public void getThirdArraysSet() {
        first = new FixedArray<>(5);
        second = new FixedArray<>(5);
        third = new FixedArray<>(5);

        first.add(0, 5);
        first.add(1, 5);
        first.add(2, 5);
        first.add(3, 6);
        first.add(4, 7);


        second.add(0, 5);
        second.add(1, 5);
        second.add(2, 5);
        second.add(3, 6);
        second.add(4, 7);

        third.add(0, 8);
        third.add(1, 8);
        third.add(2, 8);
        third.add(3, 8);
        third.add(4, 9);

    }


    @Test
    public void firstTest() {
        getFirstArraysSet();
        int expectation = 6;
        int result = finder.find(first, second, third);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void secondTest() {
        getSecondArraysSet();
        int expectation = 25;
        int result = finder.find(first, second, third);
        Assert.assertEquals(expectation, result);
    }

    @Test
    @Ignore
    public void thirdTest() {
        getThirdArraysSet();
        int expectation = 0;
        int result = finder.find(first, second, third);
        Assert.assertEquals(expectation, result);
    }
}