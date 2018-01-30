package ru.siksmfp.basic.structure.array.dynamic;

import org.junit.Assert;
import org.junit.Test;
import ru.siksmfp.basic.structure.exceptions.IncorrectIndexException;

/**
 * @author Artem Karnov @date 1/4/2018.
 * artem.karnov@t-systems.com
 */
public class DynamicArrayTest {

    @Test
    public void typicalUseCaseOne() {
        DynamicArray array = new DynamicArray();

        array.add(1);
        array.add(2);
        array.add(3);
        array.add(4);

        Assert.assertEquals(1, array.get(0));
        Assert.assertEquals(2, array.get(1));
        Assert.assertEquals(3, array.get(2));
        Assert.assertEquals(4, array.get(3));
    }

    @Test
    public void correctRemoving() {
        DynamicArray array = new DynamicArray();

        array.add(1);
        array.add(2);
        array.add(3);
        array.add(4);

        Assert.assertEquals(4, array.size());

        array.remove(0);

        Assert.assertEquals(3, array.size());
        Assert.assertEquals(2, array.get(0));
        Assert.assertEquals(3, array.get(1));
        Assert.assertEquals(4, array.get(2));
    }

    @Test(expected = IncorrectIndexException.class)
    public void failedRemovind() {
        DynamicArray array = new DynamicArray();

        array.add(10);
        array.add(9);
        array.add(8);
        array.add(7);

        array.remove(0);
        array.remove(3);
        array.remove(0);
        array.remove(10);
    }

}