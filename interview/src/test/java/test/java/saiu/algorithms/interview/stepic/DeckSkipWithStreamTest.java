package test.java.saiu.algorithms.interview.stepic;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * @author Artem Karnov @date 26.04.2017.
 * artem.karnov@t-systems.com
 */

public class DeckSkipWithStreamTest {
    @Test
    public void testOne() {
        Assert.assertEquals(com.saiu.algorithms.interview.stepic.DeckSkipWithStream
                .skipLastElements(new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5)), 2).size(), 3);
    }

}