package test.java.saiu.algorithms.general.sorts;

import org.junit.Assert;
import org.junit.Test;
import ru.siksmfp.basic.algorithm.sort.Sort;
import ru.siksmfp.basic.algorithm.sort.SortDirection;
import ru.siksmfp.basic.structure.array.dynamic.DynamicArray;

/**
 * @author Artem Karnov @date 21.04.2017.
 * artem.karnov@t-systems.com
 */

public class SortTest {

    @Test
    public void bubbleSortTestOne() {
        DynamicArray array = new DynamicArray(3, 2, 1, 4, 5);
        DynamicArray expected = new DynamicArray(1, 2, 3, 4, 5);
        Sort.bubbleSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void bubbleSortTestTwo() {
        DynamicArray array = new DynamicArray(3, 2, 1, 0, 10000, 4, 5);
        DynamicArray expected = new DynamicArray(0, 1, 2, 3, 4, 5, 10000);
        Sort.bubbleSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void bubbleSortTestThree() {
        DynamicArray array = new DynamicArray(3, 2, 1, 0, 10000, 4, 5);
        DynamicArray expected = new DynamicArray(10000, 5, 4, 3, 2, 1, 0);
        Sort.bubbleSort(array, SortDirection.DESC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void bubbleSortTestFour() {
        DynamicArray array = new DynamicArray();
        DynamicArray expected = new DynamicArray();
        Sort.bubbleSort(array, SortDirection.DESC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void selectSortTestOne() {
        DynamicArray array = new DynamicArray(3, 2, 1, 4, 5);
        DynamicArray expected = new DynamicArray(1, 2, 3, 4, 5);
        Sort.selectionSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void selectSortTestTwo() {
        DynamicArray array = new DynamicArray(3, 2, 1, 4, 5);
        DynamicArray expected = new DynamicArray(5, 4, 3, 2, 1);
        Sort.selectionSort(array, SortDirection.DESC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void selectSortTestThree() {
        DynamicArray array = new DynamicArray(3, 2, 1, 0, 10000, 4, 5);
        DynamicArray expected = new DynamicArray(0, 1, 2, 3, 4, 5, 10000);
        Sort.selectionSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void selectSortTestFour() {
        DynamicArray array = new DynamicArray();
        DynamicArray expected = new DynamicArray();
        Sort.selectionSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void insertSortTestOne() {
        DynamicArray array = new DynamicArray(3, 2, 1, 4, 5);
        DynamicArray expected = new DynamicArray(1, 2, 3, 4, 5);
        Sort.insertionSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void insertSortTesTwo() {
        DynamicArray array = new DynamicArray(3, 2, 1, 4, 5);
        DynamicArray expected = new DynamicArray(5, 4, 3, 2, 1);
        Sort.insertionSort(array, SortDirection.DESC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void insertSortTestThree() {
        DynamicArray array = new DynamicArray(3, 2, 1, 0, 10000, 4, 5);
        DynamicArray expected = new DynamicArray(0, 1, 2, 3, 4, 5, 10000);
        Sort.insertionSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void insertSortTestFour() {
        DynamicArray array = new DynamicArray();
        DynamicArray expected = new DynamicArray();
        Sort.insertionSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void insertClassicSortTestOne() {
        DynamicArray array = new DynamicArray(3, 2, 1, 4, 5);
        DynamicArray expected = new DynamicArray(1, 2, 3, 4, 5);
        Sort.classicInsertionSort(array, SortDirection.ASC);
        Assert.assertEquals(expected, array);
    }

    @Test
    public void insertClssicSortTesTwo() {
        DynamicArray array = new DynamicArray(3, 2, 1, 4, 5);
        DynamicArray expected = new DynamicArray(5, 4, 3, 2, 1);
        Sort.classicInsertionSort(array, SortDirection.DESC);
        Assert.assertEquals(expected, array);
    }
}
