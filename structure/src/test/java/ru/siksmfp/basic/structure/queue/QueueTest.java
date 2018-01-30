package ru.siksmfp.basic.structure.queue;

import org.junit.Assert;
import org.junit.Test;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;

/**
 * @author Artem Karnov @date 1/5/2018.
 * artem.karnov@t-systems.com
 */
public class QueueTest {

    @Test
    public void addElementsToQueue() {
        Queue<Integer> queue = new Queue<>();

        queue.push(1);
        queue.push(2);
        queue.push(3);
        queue.push(4);

        Assert.assertEquals(1, queue.pop().intValue());
        Assert.assertEquals(2, queue.pop().intValue());
        Assert.assertEquals(3, queue.pop().intValue());
        Assert.assertEquals(4, queue.pop().intValue());
    }

    @Test(expected = IncorrectSizeException.class)
    public void incorrectSize() {
        Queue<Integer> queue = new Queue<>();

        queue.push(1);
        queue.push(2);
        queue.push(3);
        queue.push(4);

        queue.pop();
        queue.pop();
        queue.pop();
        queue.pop();
        queue.pop();
    }

    @Test
    public void peekTest() {
        Queue<Integer> queue = new Queue<>();

        queue.push(1);
        queue.push(2);
        queue.push(3);
        queue.push(4);

        queue.peek();
        queue.peek();
        queue.peek();
        queue.peek();
        queue.peek();
        queue.peek();

        Assert.assertEquals(1, queue.peek().intValue());
    }

}