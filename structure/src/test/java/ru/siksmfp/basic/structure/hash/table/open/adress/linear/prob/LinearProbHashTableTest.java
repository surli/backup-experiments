package ru.siksmfp.basic.structure.hash.table.open.adress.linear.prob;

import org.junit.Assert;
import org.junit.Test;
import ru.siksmfp.basic.structure.api.HashTable;

/**
 * @author Artem Karnov @date 1/25/2018.
 * @email artem.karnov@t-systems.com
 */
public class LinearProbHashTableTest {
    @Test
    public void firstTest() {
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(10);
        hashTable.add(1, 1);
        hashTable.add(2, 2);
        hashTable.add(3, 3);
        hashTable.add(4, 4);
        hashTable.add(5, 5);

        Assert.assertEquals(hashTable.get(1).intValue(), 1);
        Assert.assertEquals(hashTable.get(2).intValue(), 2);
        Assert.assertEquals(hashTable.get(3).intValue(), 3);
        Assert.assertEquals(hashTable.get(4).intValue(), 4);
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
    }

    @Test
    public void secondTest() {
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(10);
        hashTable.add(1, 1);
        hashTable.add(2, 2);
        hashTable.add(3, 3);
        hashTable.add(4, 4);
        hashTable.add(5, 5);
        hashTable.add(6, 6);
        hashTable.add(7, 7);
        hashTable.add(8, 8);
        hashTable.add(9, 9);
        hashTable.add(10, 10);

        Assert.assertEquals(hashTable.get(1).intValue(), 1);
        Assert.assertEquals(hashTable.get(2).intValue(), 2);
        Assert.assertEquals(hashTable.get(3).intValue(), 3);
        Assert.assertEquals(hashTable.get(4).intValue(), 4);
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
        Assert.assertEquals(hashTable.get(6).intValue(), 6);
        Assert.assertEquals(hashTable.get(7).intValue(), 7);
        Assert.assertEquals(hashTable.get(8).intValue(), 8);
        Assert.assertEquals(hashTable.get(9).intValue(), 9);
        Assert.assertEquals(hashTable.get(10).intValue(), 10);
    }

    @Test
    public void fifthTest() {
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(10);
        hashTable.add(1, 1);
        hashTable.add(2, 2);
        hashTable.add(3, 3);
        hashTable.add(4, 4);
        hashTable.add(5, 5);

        hashTable.remove(1);
        hashTable.remove(2);

        Assert.assertNull(hashTable.get(1));
        Assert.assertNull(hashTable.get(2));
        Assert.assertEquals(hashTable.get(3).intValue(), 3);
        Assert.assertEquals(hashTable.get(4).intValue(), 4);
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
    }

    @Test
    public void sixthTest() {
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(10);
        hashTable.setHashFunction(i -> i);
        hashTable.add(1, 1);
        hashTable.add(2, 2);
        hashTable.add(3, 3);
        hashTable.add(4, 4);
        hashTable.add(5, 5);

        hashTable.remove(1);
        hashTable.remove(2);

        Assert.assertNull(hashTable.get(1));
        Assert.assertNull(hashTable.get(2));
        Assert.assertEquals(hashTable.get(3).intValue(), 3);
        Assert.assertEquals(hashTable.get(4).intValue(), 4);
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
    }

    @Test
    public void seventhTest() {
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(10);
        hashTable.setHashFunction(i -> i);

        hashTable.add(1, 1);
        hashTable.add(2, 2);
        hashTable.add(3, 3);
        hashTable.add(4, 4);
        hashTable.add(5, 5);
        hashTable.add(6, 6);
        hashTable.add(7, 7);
        hashTable.add(8, 8);
        hashTable.add(9, 9);
        hashTable.add(10, 10);

        Assert.assertEquals(hashTable.get(1).intValue(), 1);
        Assert.assertEquals(hashTable.get(2).intValue(), 2);
        Assert.assertEquals(hashTable.get(3).intValue(), 3);
        Assert.assertEquals(hashTable.get(4).intValue(), 4);
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
        Assert.assertEquals(hashTable.get(6).intValue(), 6);
        Assert.assertEquals(hashTable.get(7).intValue(), 7);
        Assert.assertEquals(hashTable.get(8).intValue(), 8);
        Assert.assertEquals(hashTable.get(9).intValue(), 9);
        Assert.assertEquals(hashTable.get(10).intValue(), 10);

        hashTable.remove(1);
        hashTable.remove(2);
        hashTable.remove(3);
        hashTable.remove(7);

        Assert.assertNull(hashTable.get(1));
        Assert.assertNull(hashTable.get(2));
        Assert.assertNull(hashTable.get(3));
        Assert.assertEquals(hashTable.get(4).intValue(), 4);
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
        Assert.assertEquals(hashTable.get(6).intValue(), 6);
        Assert.assertNull(hashTable.get(7));
        Assert.assertEquals(hashTable.get(8).intValue(), 8);
        Assert.assertEquals(hashTable.get(9).intValue(), 9);
        Assert.assertEquals(hashTable.get(10).intValue(), 10);
    }

    @Test
    public void eightTest() {
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(10);
        hashTable.setHashFunction(i -> i);
        hashTable.add(1, 1);
        hashTable.add(2, 2);
        hashTable.add(3, 3);
        hashTable.add(4, 4);
        hashTable.add(5, 5);

        hashTable.remove(1);
        hashTable.remove(2);

        Assert.assertEquals(hashTable.size(), 3);
        Assert.assertNull(hashTable.get(1));
        Assert.assertNull(hashTable.get(2));
        Assert.assertEquals(hashTable.get(3).intValue(), 3);
        Assert.assertEquals(hashTable.get(4).intValue(), 4);
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
    }

    @Test
    public void ninthTest() {
        int bigSize = 1_000_000;
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(bigSize);
        for (int i = 0; i < bigSize; i++) {
            hashTable.add(i, i * i - i);
        }

        Assert.assertEquals(hashTable.size(), bigSize);

        for (int i = 0; i < bigSize; i++) {
            Assert.assertEquals(hashTable.get(i).intValue(), i * i - i);
        }

        for (int i = 0; i < bigSize; i++) {
            hashTable.remove(i);
        }

        Assert.assertEquals(hashTable.size(), 0);
    }

    @Test
    public void tenthTest() {
        HashTable<Integer, Integer> hashTable = new LinearProbHashTable<>(10);
        hashTable.setHashFunction(i -> i);
        hashTable.add(1, 1);
        hashTable.add(2, 2);
        hashTable.add(3, 3);
        hashTable.add(4, 4);
        hashTable.add(5, 5);

        hashTable.remove(1);
        hashTable.remove(2);
        hashTable.delete(3);
        hashTable.delete(4);

        Assert.assertEquals(hashTable.size(), 3);
        Assert.assertNull(hashTable.get(1));
        Assert.assertNull(hashTable.get(2));
        Assert.assertNull(hashTable.get(3));
        Assert.assertNull(hashTable.get(4));
        Assert.assertEquals(hashTable.get(5).intValue(), 5);
    }

    @Test
    public void eleventhTest() {
        HashTable<Integer, Integer> hashTable1 = new LinearProbHashTable<>(10);
        hashTable1.add(1, 1);
        hashTable1.add(2, 2);

        HashTable<Integer, Integer> hashTable2 = new LinearProbHashTable<>(10);
        hashTable2.add(1, 1);
        hashTable2.add(2, 2);

        Assert.assertEquals(hashTable1, hashTable2);
    }

    @Test
    public void twelfthTest() {
        HashTable<Integer, Integer> hashTable1 = new LinearProbHashTable<>(10);
        hashTable1.add(1, 1);
        hashTable1.add(2, 2);

        HashTable<Integer, Integer> hashTable2 = new LinearProbHashTable<>(10);
        hashTable2.add(1, 1);
        hashTable2.add(2, 3);

        Assert.assertFalse(hashTable1.equals(hashTable2));
    }

    @Test
    public void thirteenTest() {
        HashTable<Integer, Integer> hashTable1 = new LinearProbHashTable<>(10);
        hashTable1.add(1, 1);
        hashTable1.add(2, 2);

        HashTable<Integer, Integer> hashTable2 = new LinearProbHashTable<>(10);
        hashTable2.add(1, 1);
        hashTable2.add(2, 2);

        Assert.assertEquals(hashTable1.hashCode(), hashTable2.hashCode());
    }

    @Test
    public void fourteenthTest() {
        HashTable<Integer, Integer> hashTable1 = new LinearProbHashTable<>(10);
        hashTable1.add(1, 1);
        hashTable1.add(2, 2);

        HashTable<Integer, Integer> hashTable2 = new LinearProbHashTable<>(10);
        hashTable2.add(1, 1);
        hashTable2.add(2, 3);

        Assert.assertNotEquals(hashTable1.hashCode(), hashTable2.hashCode());
    }
}