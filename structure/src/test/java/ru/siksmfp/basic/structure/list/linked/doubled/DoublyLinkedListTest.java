package ru.siksmfp.basic.structure.list.linked.doubled;

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
 * @author Artem Karnov @date 1/10/2018.
 * @email artem.karnov@t-systems.com
 */
public class DoublyLinkedListTest {

    @Test
    public void add() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(0);

        Assert.assertTrue(list.contains(0));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void get() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);

        assertEquals(1, list.get(0).intValue());
        assertEquals(4, list.get(3).intValue());
    }

    @Test(expected = IncorrectIndexException.class)
    public void incorrectGetting() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.get(-1);
    }

    @Test
    public void removeFirst() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.removeFirst();

        assertFalse(list.contains(1));
    }

    @Test(expected = IncorrectIndexException.class)
    public void removeFirstInEmptyList() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        list.removeFirst();
    }

    @Test
    public void removeLast() throws Exception {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.removeLast();

        assertFalse(list.contains(4));
    }

    @Test(expected = IncorrectIndexException.class)
    public void removeLastInEmptyList() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.removeLast();
    }

    @Test
    public void remove() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.remove(2);

        assertFalse(list.contains(3));
    }

    @Test
    public void incorrectRemoving() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(1);
        list.add(2);
        list.remove(1);
    }

    @Test
    public void isEmpty() {
        DoublyLinkedList<Integer> emptyList = new DoublyLinkedList<>();
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(1);

        assertTrue(emptyList.isEmpty());
        assertFalse(list.isEmpty());
    }

    @Test
    public void size() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        assertEquals(0, list.size());

        list.add(48);
        list.add(15);

        assertEquals(2, list.size());
    }

    @Test
    public void contains() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(48);
        list.add(15);
        list.add(16);
        list.add(23);
        list.add(42);

        assertTrue(list.contains(16));
        assertFalse(list.contains(Integer.MAX_VALUE));
    }

    @Test
    public void replaceTest() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();

        list.add(1);
        list.add(2);
        list.add(3);
        list.replace(0, 3);
        list.replace(1, 100);
        list.replace(2, null);

        Assert.assertEquals(3, list.get(0).intValue());
        Assert.assertEquals(100, list.get(1).intValue());
        Assert.assertNull(list.get(2));
    }

    @Test
    public void interationOnAllElements() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();
        ArrayStructure<Integer> array = new GArray<>();

        while (iterator.hasNext()) {
            array.add(iterator.next());
        }

        Assert.assertEquals(list.size(), array.size());
        Assert.assertEquals(list.get(0), array.get(0));
        Assert.assertEquals(list.get(1), array.get(1));
        Assert.assertEquals(list.get(2), array.get(2));
        Assert.assertEquals(list.get(3), array.get(3));
        Assert.assertEquals(list.get(4), array.get(4));
    }

    @Test
    public void addBefore() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();

        iterator.insertBefore(7);

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(2, list.get(1).intValue());
        Assert.assertEquals(7, list.get(2).intValue());
        Assert.assertEquals(3, list.get(3).intValue());
        Assert.assertEquals(4, list.get(4).intValue());
        Assert.assertEquals(5, list.get(5).intValue());
    }

    @Test
    public void addAfter() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();

        iterator.insertAfter(7);

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(2, list.get(1).intValue());
        Assert.assertEquals(3, list.get(2).intValue());
        Assert.assertEquals(7, list.get(3).intValue());
        Assert.assertEquals(4, list.get(4).intValue());
        Assert.assertEquals(5, list.get(5).intValue());
    }

    @Test
    public void isFirst() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        Assert.assertTrue(iterator.isFirst());
    }

    @Test
    public void incorrectIsFirst() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();

        Assert.assertFalse(iterator.isFirst());
    }

    @Test
    public void isLast() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        Assert.assertTrue(iterator.isLast());
    }

    @Test
    public void incorrectIsLast() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();

        Assert.assertFalse(iterator.isFirst());
    }

    @Test
    public void isLastWithInsertBefore() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.insertBefore(1);

        Assert.assertTrue(iterator.isLast());
    }

    @Test
    public void isLastWithInsertAfter() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        iterator.insertAfter(1);

        Assert.assertFalse(iterator.isLast());
    }

    @Test
    public void removeBefore() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();
        iterator.removeBefore();

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(3, list.get(1).intValue());
        Assert.assertEquals(4, list.get(2).intValue());
        Assert.assertEquals(5, list.get(3).intValue());
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void removeBeforeSecond() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        iterator.removeBefore();

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(2, list.get(1).intValue());
        Assert.assertEquals(3, list.get(2).intValue());
        Assert.assertEquals(5, list.get(3).intValue());
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void removeBeforeAndAddBefore() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

        iterator.removeBefore();
        iterator.insertBefore(4);

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(2, list.get(1).intValue());
        Assert.assertEquals(3, list.get(2).intValue());
        Assert.assertEquals(4, list.get(3).intValue());
        Assert.assertEquals(5, list.get(4).intValue());
        Assert.assertEquals(5, list.size());
    }

    @Test
    public void removeAfter() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.next();
        iterator.next();
        iterator.removeAfter();

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(2, list.get(1).intValue());
        Assert.assertEquals(3, list.get(2).intValue());
        Assert.assertEquals(5, list.get(3).intValue());
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void removeAfterSecond() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.removeAfter();

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(3, list.get(1).intValue());
        Assert.assertEquals(4, list.get(2).intValue());
        Assert.assertEquals(5, list.get(3).intValue());
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void removeAfterAndAddAfter() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = list.getIterator();

        iterator.removeAfter();
        iterator.insertAfter(2);

        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(2, list.get(1).intValue());
        Assert.assertEquals(3, list.get(2).intValue());
        Assert.assertEquals(4, list.get(3).intValue());
        Assert.assertEquals(5, list.get(4).intValue());
        Assert.assertEquals(5, list.size());
    }

    @Test
    public void getLast() {
        DoublyLinkedList<Integer> doublyLinkedList = new DoublyLinkedList<>(1, 2, 3, 4, 5);
        Assert.assertEquals(5, doublyLinkedList.getLast().intValue());
    }

    @Test(expected = IncorrectIndexException.class)
    public void getLastUnsuccessfully() {
        DoublyLinkedList<Integer> doublyLinkedList = new DoublyLinkedList<>();
        doublyLinkedList.getLast();
    }
}