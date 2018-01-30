package ru.siksmfp.basic.structure.list.linked.simple;

import org.junit.Assert;
import org.junit.Test;
import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.api.Iterator;
import ru.siksmfp.basic.structure.array.g.GArray;
import ru.siksmfp.basic.structure.exceptions.IncorrectIndexException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Artem Karnov @date 29.11.2016.
 * artem.karnov@t-systems.com
 **/
public class SimpleLinkedListTest {

    @Test
    public void add() {
        SimpleLinkedList<Integer> list = new SimpleLinkedList<Integer>();

        list.add(0);
        Assert.assertTrue(list.contains(0));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void get() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.add(1);
        simpleLinkedList.add(2);
        simpleLinkedList.add(3);
        simpleLinkedList.add(4);

        assertEquals(1, simpleLinkedList.get(0).intValue());
        assertEquals(4, simpleLinkedList.get(3).intValue());
    }

    @Test(expected = IncorrectIndexException.class)
    public void incorrectGetting() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.get(-1);
        simpleLinkedList.get(4);
    }

    @Test
    public void removeFirst() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.add(1);
        simpleLinkedList.add(2);
        simpleLinkedList.add(3);
        simpleLinkedList.add(4);
        simpleLinkedList.removeFirst();

        assertFalse(simpleLinkedList.contains(1));
    }

    @Test(expected = IncorrectIndexException.class)
    public void removeFirstInEmptyList() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();
        simpleLinkedList.removeFirst();
    }

    @Test
    public void removeLast() throws Exception {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.add(1);
        simpleLinkedList.add(2);
        simpleLinkedList.add(3);
        simpleLinkedList.add(4);
        simpleLinkedList.removeLast();

        assertFalse(simpleLinkedList.contains(4));
    }

    @Test(expected = IncorrectIndexException.class)
    public void removeLastInEmptyList() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();
        simpleLinkedList.removeLast();
    }

    @Test
    public void remove() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.add(1);
        simpleLinkedList.add(2);
        simpleLinkedList.add(3);
        simpleLinkedList.add(4);
        simpleLinkedList.remove(2);

        assertFalse(simpleLinkedList.contains(3));
    }

    @Test
    public void incorrectRemoving() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.add(1);
        simpleLinkedList.add(2);
        simpleLinkedList.remove(1);
    }

    @Test
    public void isEmpty() {
        SimpleLinkedList<Integer> emptyList = new SimpleLinkedList<>();
        SimpleLinkedList<Integer> list = new SimpleLinkedList<>();

        list.add(1);

        assertTrue(emptyList.isEmpty());
        assertFalse(list.isEmpty());
    }

    @Test
    public void size() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        assertEquals(0, simpleLinkedList.size());

        simpleLinkedList.add(48);
        simpleLinkedList.add(15);

        assertEquals(2, simpleLinkedList.size());
    }

    @Test
    public void contains() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.add(48);
        simpleLinkedList.add(15);
        simpleLinkedList.add(16);
        simpleLinkedList.add(23);
        simpleLinkedList.add(42);

        assertTrue(simpleLinkedList.contains(16));
        assertFalse(simpleLinkedList.contains(Integer.MAX_VALUE));
    }

    @Test
    public void replaceTest() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();

        simpleLinkedList.add(1);
        simpleLinkedList.add(2);
        simpleLinkedList.add(3);
        simpleLinkedList.replace(0, 3);
        simpleLinkedList.replace(1, 100);
        simpleLinkedList.replace(2, null);

        Assert.assertEquals(3, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(100, simpleLinkedList.get(1).intValue());
        Assert.assertNull(simpleLinkedList.get(2));
    }

    @Test
    public void interationOnAllElements() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();
        ArrayStructure<Integer> array = new GArray<>();

        while (iterator.hasNext()) {
            array.add(iterator.next());
        }

        Assert.assertEquals(simpleLinkedList.size(), array.size());
        Assert.assertEquals(simpleLinkedList.get(0), array.get(0));
        Assert.assertEquals(simpleLinkedList.get(1), array.get(1));
        Assert.assertEquals(simpleLinkedList.get(2), array.get(2));
        Assert.assertEquals(simpleLinkedList.get(3), array.get(3));
        Assert.assertEquals(simpleLinkedList.get(4), array.get(4));
    }

    @Test
    public void addBefore() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();

        iterator.insertBefore(7);

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(2, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(7, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(4, simpleLinkedList.get(4).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(5).intValue());
    }

    @Test
    public void addAfter() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();

        iterator.insertAfter(7);

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(2, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(7, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(4, simpleLinkedList.get(4).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(5).intValue());
    }

    @Test
    public void isFirst() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        Assert.assertTrue(iterator.isFirst());
    }

    @Test
    public void incorrectIsFirst() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();

        Assert.assertFalse(iterator.isFirst());
    }

    @Test
    public void isLast() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        Assert.assertTrue(iterator.isLast());
    }

    @Test
    public void incorrectIsLast() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();

        Assert.assertFalse(iterator.isFirst());
    }

    @Test
    public void isLastWithInsertBefore() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.insertBefore(1);

        Assert.assertTrue(iterator.isLast());
    }

    @Test
    public void isLastWithInsertAfter() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        iterator.insertAfter(1);

        Assert.assertFalse(iterator.isLast());
    }

    @Test
    public void removeBefore() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();
        iterator.removeBefore();

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(4, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(4, simpleLinkedList.size());
    }

    @Test
    public void removeBeforeSecond() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        iterator.removeBefore();

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(2, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(4, simpleLinkedList.size());
    }

    @Test
    public void removeBeforeAndAddBefore() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        iterator.removeBefore();
        iterator.insertBefore(4);

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(2, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(4, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(4).intValue());
        Assert.assertEquals(5, simpleLinkedList.size());
    }

    @Test
    public void removeAfter() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.next();
        iterator.next();
        iterator.removeAfter();

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(2, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(4, simpleLinkedList.size());
    }

    @Test
    public void removeAfterSecond() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();

        iterator.removeAfter();

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(4, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(4, simpleLinkedList.size());
    }

    @Test
    public void removeAfterAndAddAfter() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = simpleLinkedList.getIterator();


        iterator.removeAfter();
        iterator.insertAfter(2);

        Assert.assertEquals(1, simpleLinkedList.get(0).intValue());
        Assert.assertEquals(2, simpleLinkedList.get(1).intValue());
        Assert.assertEquals(3, simpleLinkedList.get(2).intValue());
        Assert.assertEquals(4, simpleLinkedList.get(3).intValue());
        Assert.assertEquals(5, simpleLinkedList.get(4).intValue());
        Assert.assertEquals(5, simpleLinkedList.size());
    }

    @Test
    public void getLast() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>(1, 2, 3, 4, 5);
        Assert.assertEquals(5, simpleLinkedList.getLast().intValue());
    }

    @Test(expected = IncorrectIndexException.class)
    public void getLastUnsuccessfully() {
        SimpleLinkedList<Integer> simpleLinkedList = new SimpleLinkedList<>();
        simpleLinkedList.getLast();
    }
}