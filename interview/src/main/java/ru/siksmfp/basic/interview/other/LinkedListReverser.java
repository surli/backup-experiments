package ru.siksmfp.basic.interview.other;


/**
 * @author Artem Karnov @date 25.02.17.
 * artem.karnov@t-systems.com
 */

import java.util.LinkedList;

/**
 * Reversal one-way linked list
 * listUnpacking and listPacking are needed for transformation
 * SimpleLinkedList structure into internal list.
 * Without it algorithm doesn't matter as method for self education
 */

public class LinkedListReverser<T> {
    private int linkedListSize;

    /**
     * Method gets linked list and reverses it
     * without allocating new memory
     *
     * @param sourceList linked list for reversing
     * @return reversed linked list
     */
    public LinkedList<T> reverse(LinkedList<T> sourceList) {
        List initialList = listUnpacking(sourceList),
                selfList = initialList,
                previous = null,
                resultRoot = initialList,
                newList = null;
        boolean isFirstEntryToLoop = true;
        while (selfList.next != null) {
            while (selfList.next != null) {
                previous = selfList;
                selfList = selfList.next;
            }
            if (isFirstEntryToLoop) {
                selfList.next = null;
                newList = selfList;
                resultRoot = newList;
                newList.next = previous;
                previous.next = null;
                newList = newList.next;
                isFirstEntryToLoop = false;
            } else {
                newList.next = previous;
                newList = newList.next;
                previous.next = null;
            }
            selfList = initialList;

        }

        sourceList = listPacking(resultRoot);
        return sourceList;
    }

    /**
     * Method gets linked list and reverses it.
     * But it uses addition allocated memory.
     * It's impossible to create method without memory allocation in all.
     * Just manipulating with links in charge to do it.
     *
     * @param sourceList linked list for reversing
     * @return reversed linked list
     */
    @Deprecated
    public LinkedList<T> oldReverseRealizaton(LinkedList<T> sourceList) {
        List currentListRoot = listUnpacking(sourceList),
                resultRoot = null,
                previous = null,
                newList = new List(),
                selfList = currentListRoot;
        boolean resultRootInitializeMark = true;

        while (selfList.next != null) {
            while (selfList.next != null) {
                previous = selfList;
                selfList = selfList.next;
            }
            previous.next = null;
            newList.data = previous.data;
            if (resultRootInitializeMark) {
                resultRoot = newList;
                resultRootInitializeMark = false;
            }
            if (currentListRoot.next != null) {
                newList.next = new List();
            }
            newList = newList.next;
            selfList = currentListRoot;
        }
        sourceList = listPacking(resultRoot);
        return sourceList;
    }

    /**
     * Transformation linked list data structure into list-based structure
     * Needed for algorithm purposes
     *
     * @param linkedList linked list data structure
     * @return list-based structure
     */
    private List<T> listUnpacking(LinkedList<T> linkedList) {
        List<T> sefList = new List<>();
        List<T> root;
        root = sefList;
        linkedListSize = linkedList.size();
        for (int i = 0; i < linkedListSize; i++) {
            sefList.data = linkedList.get(i);
            if (i < linkedList.size() - 1) {
                List<T> nextList = new List<>();
                sefList.next = nextList;
                sefList = sefList.next;
            }
        }
        return root;
    }

    /**
     * Transformation list-based structure into linked list structure
     * Needed for test purposes
     *
     * @param selfList first list of structure
     * @return linked list data structure
     */
    private LinkedList<T> listPacking(List<T> selfList) {
        LinkedList<T> linkedList = new LinkedList<T>();
        if (selfList.next == null && selfList.data == null)
            return linkedList;
        for (int i = 0; selfList != null; i++) {
            linkedList.add(selfList.data);
            selfList = selfList.next;
        }
        return linkedList;
    }

    /**
     * Class for linking lists
     *
     * @param <T>
     */
    private class List<T> {
        public T data;
        public List next;

        public List() {
            next = null;
        }
    }

}
