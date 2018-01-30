package ru.siksmfp.basic.algorithm.parse;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Artem Karnov @date 1/6/2018.
 * artem.karnov@t-systems.com
 */
@Ignore
public class ArithmeticParserTest {
    private static final double DELTA = 0.0001;
    private ArithmeticParser arithmeticParser = new ArithmeticParser();

    @Test
    public void testOne() {
        double result = arithmeticParser.parse("1+1");
        double expected = 2;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Test
    public void testTwo() {
        double result = arithmeticParser.parse("1+1+1");
        double expected = 3;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Test
    public void testThree() {
        double result = arithmeticParser.parse("(1+1)+1");
        double expected = 3;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Test
    @Ignore
    public void testFour() {
        double result = arithmeticParser.parse("7+5-1*(2-3+7)");
        double expected = 6;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Test
    public void testFive() {
        double result = arithmeticParser.parse("4^0.5");
        double expected = 2;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Test
    public void testSix() {
        double result = arithmeticParser.parse("1^(1^1)");
        double expected = 1;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Ignore
    @Test
    public void testSeven() {
        double result = arithmeticParser.parse("25/(1+1+(2*2)/2+1)");
        double expected = 5;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Ignore
    @Test
    public void testEight() {
        double result = arithmeticParser.parse("5*25^0.5)");
        double expected = 25;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Ignore
    @Test
    public void testNine() {
        double result = arithmeticParser.parse("7*0.3 + 999)");
        double expected = 1001.1;
        Assert.assertEquals(expected, result, DELTA);
    }

    @Ignore
    @Test
    public void testTen() {
        double result = arithmeticParser.parse("25 * 0.2/5)");
        double expected = 1;
        Assert.assertEquals(expected, result, DELTA);
    }
}