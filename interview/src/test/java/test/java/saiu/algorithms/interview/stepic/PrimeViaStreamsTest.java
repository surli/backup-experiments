package test.java.saiu.algorithms.interview.stepic;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Artem Karnov @date 21.04.2017.
 * artem.karnov@t-systems.com
 */

public class PrimeViaStreamsTest {

    @Test
    public void firstTest() {
        Assert.assertTrue(com.saiu.algorithms.interview.stepic.PrimeNumbersViaStreams.isPrime(5));
    }

    @Test
    public void secondTest() {
        Assert.assertTrue(com.saiu.algorithms.interview.stepic.PrimeNumbersViaStreams.isPrime(3));
    }

    @Test
    public void thirdTest() {
        Assert.assertTrue(com.saiu.algorithms.interview.stepic.PrimeNumbersViaStreams.isPrime(7));
    }

    @Test
    public void fourthTest() {
        Assert.assertTrue(com.saiu.algorithms.interview.stepic.PrimeNumbersViaStreams.isPrime(337));
    }

    @Test
    public void fifthTest() {
        Assert.assertFalse(com.saiu.algorithms.interview.stepic.PrimeNumbersViaStreams.isPrime(10));
    }

}