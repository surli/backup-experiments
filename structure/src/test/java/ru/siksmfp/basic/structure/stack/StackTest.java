package ru.siksmfp.basic.structure.stack;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;

/**
 * Created by Artyom Karnov on 18.11.16.
 * artyom-karnov@yandex.ru
 **/
public class StackTest {
    Stack<Integer> stack;

    @Before
    public void setUp() throws Exception {
        stack = new Stack();
        stack.push(0);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);
        stack.push(5);
    }

    @Test
    public void sizeTest() {
        Assert.assertEquals(stack.size(), 6);
    }

    @Test
    public void sizeTestTwo() {
        Assert.assertNotEquals(stack.size(), -5);
    }

    @Test
    public void pushTestOne() {
        Assert.assertNotEquals(stack.pop(), Integer.valueOf(6));
    }

    @Test
    public void pushTestTwo() {
        stack.push(100);
        Assert.assertEquals(stack.pop().intValue(), 100);
    }

    @Test
    public void popTestOne() {
        Assert.assertEquals(stack.pop().intValue(), 5);
        Assert.assertEquals(stack.pop().intValue(), 4);
        Assert.assertEquals(stack.pop().intValue(), 3);
        Assert.assertEquals(stack.pop().intValue(), 2);
        Assert.assertEquals(stack.pop().intValue(), 1);
        Assert.assertEquals(stack.pop().intValue(), 0);
    }

    @Test(expected = IncorrectSizeException.class)
    public void popTestTwo() {
        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();
    }

    @Test
    public void containTestOne() {
        Assert.assertTrue(stack.contains(3));
    }

    @Test
    public void containTestTwo() {
        Assert.assertFalse(stack.contains(33));
    }

}