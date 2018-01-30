package ru.siksmfp.basic.structure.utils.math;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Artem Karnov @date 1/25/2018.
 * @email artem.karnov@t-systems.com
 */
public class MathTest {

    @Test
    public void firstTest() {
        Assert.assertEquals(Math.getFirstSimpleNumberAfter(1299689), 1299709);
    }

    @Test
    public void secondTest() {
        Assert.assertEquals(Math.getFirstSimpleNumberAfter(1299437), 1299439);
    }

    @Test
    public void thirdTest() {
        Assert.assertEquals(Math.getFirstSimpleNumberAfter(15), 17);
    }

    @Test
    public void fourthTest() {
        Assert.assertEquals(Math.getFirstSimpleNumberAfter(-1), 1);
    }

    @Test
    public void fifthTest() {
        Assert.assertEquals(Math.getFirstSimpleNumberAfter(1), 3);
    }

    @Test
    public void sixthTest() {
        Assert.assertEquals(Math.getFirstSimpleNumberAfter(9277), 9281);
    }
}