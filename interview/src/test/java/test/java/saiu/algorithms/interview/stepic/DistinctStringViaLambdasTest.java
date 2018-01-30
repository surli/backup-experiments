package test.java.saiu.algorithms.interview.stepic;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Artem Karnov @date 25.04.2017.
 * artem.karnov@t-systems.com
 */

public class DistinctStringViaLambdasTest {
    @Test
    public void testOne() {
        List<String> list = Arrays.asList("java", "scala", "java", "clojure", "clojure");
        List<String> expected = Arrays.asList("clojure", "java", "scala");
        List<String> result = com.saiu.algorithms.interview.stepic.DistinctStringViaLambdas.distinctString(list);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testTwo() {
        List<String> list = Arrays.asList("the", "three", "the",
                "three", "the", "three", "an", "an", "a");
        List<String> expected = Arrays.asList("a", "an", "the", "three");
        List<String> result = com.saiu.algorithms.interview.stepic.DistinctStringViaLambdas.distinctString(list);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testThree() {
        List<String> list = Arrays.asList("java", "scala", "java", "clojure", "clojure");
        List<String> expected = Arrays.asList("clojure", "java", "scala");
        List<String> result = com.saiu.algorithms.interview.stepic.DistinctStringViaLambdas.distinctStringWithStream(list);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testFour() {
        List<String> list = Arrays.asList("the", "three", "the",
                "three", "the", "three", "an", "an", "a");
        List<String> expected = Arrays.asList("a", "an", "the", "three");
        List<String> result = com.saiu.algorithms.interview.stepic.DistinctStringViaLambdas.distinctStringWithStream(list);
        Assert.assertEquals(expected, result);
    }

}