package ru.siksmfp.basic.structure.array.fixed;

import org.junit.Assert;
import org.junit.Test;
import ru.siksmfp.basic.structure.exceptions.IncorrectIndexException;

/**
 * @author Artem Karnov @date 1/4/2018.
 * artem.karnov@t-systems.com
 */
public class FixedArrayTest {

    @Test
    public void typicalUseCaseOne() {
        FixedArray<Integer> array = new FixedArray<>(5);

        array.add(0, 0);
        array.add(1, 1);
        array.add(2, 2);
        array.add(3, 3);
        array.add(4, 4);

        Assert.assertEquals(0, array.get(0).intValue());
        Assert.assertEquals(1, array.get(1).intValue());
        Assert.assertEquals(2, array.get(2).intValue());
        Assert.assertEquals(3, array.get(3).intValue());
        Assert.assertEquals(4, array.get(4).intValue());
    }

    @Test(expected = IncorrectIndexException.class)
    public void incorrectReferringToIndex() {
        FixedArray<Integer> array = new FixedArray<>(1);
        array.get(2);
    }

    @Test
    public void arrayContainsElement() {
        FixedArray<Integer> array = new FixedArray<>(5);

        array.add(0, 0);
        array.add(1, 1);
        array.add(2, 2);
        array.add(3, 3);
        array.add(4, 4);

        Assert.assertTrue(array.contains(2));
        Assert.assertTrue(array.contains(3));
        Assert.assertFalse(array.contains(300));
    }

    @Test
    public void correctRemoving() {
        FixedArray<Integer> array = new FixedArray<>(5);

        array.add(0, 0);
        array.add(1, 1);
        array.add(2, 2);
        array.add(3, 3);
        array.add(4, 4);

        array.remove(0);

        Assert.assertEquals(1, array.get(0).intValue());
        Assert.assertEquals(2, array.get(1).intValue());
        Assert.assertEquals(3, array.get(2).intValue());
        Assert.assertEquals(4, array.get(3).intValue());
        Assert.assertEquals(null, array.get(4));

    }

    @Test(expected = IncorrectIndexException.class)
    public void failedRemoving() {
        FixedArray<Integer> array = new FixedArray<>(0);

        array.remove(1);
    }
}