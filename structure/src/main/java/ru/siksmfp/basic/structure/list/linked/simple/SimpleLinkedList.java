package ru.siksmfp.basic.structure.list.linked.simple;

import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.api.Iterator;
import ru.siksmfp.basic.structure.api.ListStructure;
import ru.siksmfp.basic.structure.exceptions.IncorrectIndexException;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;
import ru.siksmfp.basic.structure.utils.StructureUtils;
import ru.siksmfp.basic.structure.utils.SystemUtils;

/**
 * Created by Artyom Karnov on 23.11.16.
 * artyom-karnov@yandex.ru
 * <p>
 * Typical linkedList
 *
 * @param <T> object type for storing in linkedList
 */
public class SimpleLinkedList<T> implements ListStructure<T> {
    /**
     * Class represents list of elements
     *
     * @param <T> object type for storing in list
     */
    private static class List<T> {
        private T data;
        private List<T> next;

        public List() {
            next = null;
        }

        public List(T data) {
            this.data = data;
            next = null;
        }
    }

    /**
     * Iterator for simple linked list
     */
    private class ListIterator implements Iterator<T> {
        private List<T> currentList;
        private List<T> firstList;
        private int currentPosition;

        public ListIterator(List<T> fistList) {
            this.currentList = fistList;
            this.firstList = fistList;
        }

        /**
         * Is there next element?
         *
         * @return true if there is next element, false if there isn't
         */
        @Override
        public boolean hasNext() {
            return currentPosition != size;
        }

        /**
         * Is current element last?
         *
         * @return true if current element is last element in the list
         * false if it isn't
         */
        @Override
        public boolean isLast() {
            return currentPosition == size - 1;
        }

        /**
         * Is current element first?
         *
         * @return true if current element is first element in the list
         * false if it isn't
         */
        @Override
        public boolean isFirst() {
            return currentPosition == 0;
        }

        /**
         * Rollback current pointer to first list's element
         */
        @Override
        public void reset() {
            currentList = this.firstList;
            currentPosition = 0;
        }

        /**
         * Get data of current element and move pointer to next element
         *
         * @return data of current element
         */
        @Override
        public T next() {
            T data = currentList.data;
            currentList = currentList.next;
            currentPosition++;
            return data;
        }

        /**
         * Insert element before current
         *
         * @param element element for inserting
         */
        @Override
        public void insertBefore(T element) {
            List<T> newList = new List<>();
            newList.data = element;
            if (currentPosition == 0) {
                newList.next = firstList;
                firstList = newList;
            } else {
                List<T> tempList = firstList;
                for (int i = 0; i < currentPosition - 1; i++) {
                    tempList = tempList.next;
                }
                tempList.next = newList;
                newList.next = currentList;
            }
            size++;
            currentPosition++;
        }

        /**
         * Insert element after current
         *
         * @param element element for inserting
         */
        @Override
        public void insertAfter(T element) {
            List<T> newList = new List<>(element);
            newList.next = currentList.next;
            currentList.next = newList;
            size++;
        }

        /**
         * Replace data in current element
         *
         * @param element element for replacing
         */
        @Override
        public void replace(T element) {
            currentList.data = element;
        }

        /**
         * Insert element before current strictly
         *
         * @param element element for inserting
         */
        @Override
        public void strictInsertBefore(T element) {
            List<T> newList = new List<>();
            newList.data = (T) SystemUtils.clone(element);
            if (currentPosition == 0) {
                newList.next = firstList;
                firstList = newList;
            } else {
                List<T> tempList = firstList;
                for (int i = 0; i < currentPosition - 1; i++) {
                    tempList = tempList.next;
                }
                tempList.next = newList;
                newList.next = currentList;
            }
            size++;
            currentPosition++;
        }

        /**
         * Insert element after current strictly
         *
         * @param element element for inserting
         */
        @Override
        public void strictInsertAfter(T element) {
            List<T> newList = new List<>();
            newList.data = (T) SystemUtils.clone(element);
            newList.next = currentList.next;
            currentList.next = newList;
            size++;
        }

        /**
         * Replace data in current element strictly
         *
         * @param element element for replacing
         */
        @Override
        public void strictReplace(T element) {
            currentList.data = (T) SystemUtils.clone(element);
        }

        /**
         * Remove element before current
         *
         * @throws IncorrectSizeException if current element is first
         */
        @Override
        public void removeBefore() {
            if (currentPosition == 0) {
                throw new IncorrectSizeException("There is not element before");
            } else {
                List<T> tempList = firstList;
                for (int i = 1; i < currentPosition - 1; i++) {
                    tempList = tempList.next;
                }
                tempList.next = currentList;
                currentPosition--;
                size--;
            }
        }

        /**
         * Remove element after current
         *
         * @throws IncorrectSizeException if current element is last
         */
        @Override
        public void removeAfter() {
            if (currentPosition == size - 1) {
                throw new IncorrectSizeException("There is not element after");
            } else {
                currentList.next = currentList.next.next;
                size--;
            }
        }

        /**
         * Remove current element
         */
        @Override
        public void remove() {
            currentList = currentList.next;
            size--;
            currentPosition--;
        }

        /**
         * Set to current element's data NULL value
         */
        @Override
        public void delete() {
            currentList.data = null;
        }
    }

    private List<T> firstList;
    private int size;

    /**
     * Constructor with begin initialization
     */
    public SimpleLinkedList() {
        size = 0;
    }

    /**
     * Add elements to linked list
     *
     * @param objects objects for adding
     */
    public SimpleLinkedList(T... objects) {
        size = 0;
        for (T object : objects) {
            add(object);
        }
    }

    /**
     * Creating simple linked list from array structure
     *
     * @param arrayStructure structure for creating
     */
    public SimpleLinkedList(ArrayStructure<T> arrayStructure) {
        size = 0;
        for (int i = 0; i < arrayStructure.size(); i++) {
            add(arrayStructure.get(i));
        }
    }

    /**
     * Adding element to linkedList
     *
     * @param element element for adding
     */
    @Override
    public void add(T element) {
        StructureUtils.checkDataStructureSize(size);
        if (firstList == null) {
            firstList = new List<>(element);
        } else {
            List tempList = firstList;
            while (tempList.next != null) {
                tempList = tempList.next;
            }
            tempList.next = new List();
            tempList.next.data = element;
        }
        size++;
    }

    /**
     * Adding element to linkedList strictly
     *
     * @param element element for adding
     */
    @Override
    public void strictAdd(T element) {
        StructureUtils.checkDataStructureSize(size);
        if (firstList == null) {
            firstList = new List<>((T) SystemUtils.clone(element));
        } else {
            List tempList = firstList;
            while (tempList.next != null) {
                tempList = tempList.next;
            }
            tempList.next = new List();
            tempList.next.data = element;
        }
        size++;
    }

    /**
     * Replace element in linked list
     *
     * @param index   index of element
     * @param element element for replacment
     */
    @Override
    public void replace(int index, T element) {
        StructureUtils.checkingIndex(index, size);
        List tempList = firstList;
        for (int i = 0; i < index; i++) {
            tempList = tempList.next;
        }
        tempList.data = element;
    }

    /**
     * Replace element in linked list strictly
     *
     * @param index   index of element
     * @param element element for replacment
     */
    @Override
    public void strictReplace(int index, T element) {
        StructureUtils.checkingIndex(index, size);
        List tempList = firstList;
        for (int i = 0; i < index; i++) {
            tempList = tempList.next;
        }
        tempList.data = SystemUtils.clone(element);
    }

    /**
     * Getting element from adjusted position
     *
     * @param index index of element in linkedList
     * @return element from adjusted position
     */
    @Override
    public T get(int index) {
        StructureUtils.checkingIndex(index, size);
        List<T> tempList = firstList;
        for (int i = 0; i < index; i++) {
            tempList = tempList.next;
        }
        return tempList.data;
    }

    /**
     * Get first element of linked list
     *
     * @return first element of linked list
     */
    @Override
    public T getFirst() {
        if (size > 0) {
            return firstList.data;
        } else {
            throw new IncorrectSizeException("List is empty");
        }
    }

    /**
     * Get last element of linked list
     *
     * @return last element of linked list
     */
    @Override
    public T getLast() {
        if (size() > 1) {
            List<T> tempList = firstList;
            while (tempList.next != null) {
                tempList = tempList.next;
            }
            return tempList.data;
        } else if (size == 1)
            return getFirst();
        else {
            throw new IncorrectIndexException("List is empty");
        }
    }

    /**
     * Method for displaying all lists of liked list
     */
    @SuppressWarnings("Warning")
    @Override
    public T[] toArray() {
        Object[] arr = new Object[size];
        List tempList = firstList;
        int i = 0;
        while (tempList.next != null) {
            arr[i] = tempList.data;
            tempList = tempList.next;
        }
        return (T[]) arr;
    }

    /**
     * Removing element first element
     */
    @Override
    public void removeFirst() {
        if (size() > 0) {
            firstList = firstList.next;
            size--;
        } else {
            throw new IncorrectIndexException("List is already empty");
        }
    }

    /**
     * Removing element with last index
     */
    @Override
    public void removeLast() {
        if (size() > 1) {
            List tempList = firstList;
            while (tempList.next.next != null) {
                tempList = tempList.next;
            }
            tempList.next = null;
            size--;
        } else if (size == 1)
            removeFirst();
        else {
            throw new IncorrectIndexException("List is empty");
        }
    }

    /**
     * Removing element with adjusted index
     *
     * @param index index for removing
     */

    /*
     * There are 3 situations when is needed to remove list from com.saiu.ru.siksmfp.basic.structure.list.linked.binary
     * 1 - needed element is first - checking by "if"
     * ([X][][][][])
     * 2 - is needed to remove last element - checking by "else if"
     * ([][][][][X])
     * 3 - is needed to remove element which "surrounded" by others - on last "else"
     * ([][][X][][])
     */
    @Override
    public void remove(int index) {
        StructureUtils.checkingIndex(index, size);
        if (index == 0) {
            removeFirst();
        } else {
            List tempList = firstList;
            for (int i = 0; i < index - 1; i++) {
                tempList = tempList.next;
            }
            size--;
            tempList.next = tempList.next.next;
        }
    }

    /**
     * Replace element with @index on NULL value
     *
     * @param index index of replacement element
     */
    @Override
    public void delete(int index) {
        StructureUtils.checkingIndex(index, size);
        List<T> tempList = firstList;
        for (int i = 0; i < index; i++) {
            tempList = tempList.next;
        }
        tempList.data = null;
    }

    /**
     * Checking linkedList on elements presence
     *
     * @return true - if linkedList has elements, false - if linkedList is empty
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Get iterator of linked list
     *
     * @return
     */
    @Override
    public Iterator<T> getIterator() {
        return new ListIterator(firstList);
    }

    /**
     * Getting size of linkedList
     *
     * @return number of elements in linked list
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Checking elements storing in linkedList
     *
     * @param element element for checking
     * @return true - if array contain adjusted element, false - if doesn't
     */
    @Override
    public boolean contains(T element) {
        List tempList = firstList;
        do {
            if (tempList.data.equals(element)) {
                return true;
            }
            tempList = tempList.next;
        }
        while (tempList.next != null);
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof SimpleLinkedList)) return false;
        SimpleLinkedList<T> list1 = (SimpleLinkedList<T>) o;
        if (size != list1.size) return false;

        List tempList = firstList;
        List tempList1 = list1.firstList;
        while (tempList.next != null) {
            if (tempList.data == null || tempList1.data == null) {
                if (tempList.data == null && tempList1.data != null)
                    return false;
                if (tempList.data != null && tempList1.data == null)
                    return false;
            } else {
                if (!tempList.data.equals(tempList1.data))
                    return false;
            }
            tempList = tempList.next;
            tempList1 = tempList1.next;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * size;
        for (int i = 0; i < size; i++) {
            if (firstList != null) {
                List tempList = firstList;
                while (tempList.next != null) {
                    result += 31 * tempList.data.hashCode();
                    tempList = tempList.next;
                }
            } else {
                result += 31;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        List tempList = firstList;
        while (tempList != null) {
            result.append(tempList.data);
            result.append(", ");
            tempList = tempList.next;
        }
        if (result.length() > 1) {
            result.delete(result.length() - 2, result.length());
        }
        return "SimpleLinkedList{" + result.toString() + "}";
    }
}
