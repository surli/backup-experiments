package ru.siksmfp.basic.structure.list.linked.g;

import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.api.Iterator;
import ru.siksmfp.basic.structure.api.ListStructure;
import ru.siksmfp.basic.structure.exceptions.IncorrectIndexException;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;
import ru.siksmfp.basic.structure.utils.StructureUtils;
import ru.siksmfp.basic.structure.utils.SystemUtils;

/**
 * @author Artem Karnov @date 1/8/2018.
 * artem.karnov@t-systems.com
 */
public class GLinkedList<T> implements ListStructure<T> {
    /**
     * Class represents list of elements
     *
     * @param <T> object type for storing in list
     */
    private class List<T> {
        public T data;
        public List next;
        public List previous;

        public List() {
        }

        /**
         * Add pointer on previous element
         *
         * @param previous element for adding
         */
        public void addPrevious(List<T> previous) {
            this.previous = previous;
        }

        /**
         * Add pointer on next element
         *
         * @param next element for adding
         */
        public void addNext(List<T> next) {
            this.next = next;
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
            return currentPosition < size;
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
                firstList.previous = newList;
                firstList = newList;
            } else {
                currentList.previous.next = newList;
                newList.next = currentList;
                currentList.previous = newList;
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
            List<T> newList = new List<>();
            newList.data = element;
            newList.next = currentList.next;
            currentList.next = newList;
            if (currentPosition == size - 1) {
                lastList = newList;
            }
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
                firstList.previous = newList;
                firstList = newList;
            } else {
                currentList.previous.next = newList;
                newList.next = currentList;
                currentList.previous = newList;
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
            if (currentPosition == size - 1) {
                lastList = newList;
            }
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
                List<T> previous = currentList.previous;
                if (previous.previous != null) {
                    previous.previous.next = currentList;
                    currentList.previous = previous.previous;
                } else {
                    currentList.previous = null;
                }
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
            if (currentPosition == 0) {
                throw new IncorrectSizeException("There is not element before");
            } else {
                currentList = currentList.next;
                size--;
                currentPosition--;
            }
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
    private List<T> lastList;
    private int size;

    /**
     * Constructor with begin initialization
     */
    public GLinkedList() {
        size = 0;
        firstList = null;
    }

    /**
     * Add elements to linked list
     *
     * @param objects objects for adding
     */
    public GLinkedList(T... objects) {
        for (T object : objects) {
            add(object);
        }
    }

    /**
     * Creating simple linked list from array structure
     *
     * @param arrayStructure structure for creating
     */
    public GLinkedList(ArrayStructure<T> arrayStructure) {
        for (int i = 0; i < arrayStructure.size(); i++) {
            add(arrayStructure.get(i));
        }
    }

    /**
     * Adding element to g linked list
     *
     * @param element element for adding
     */
    public void add(T element) {
        StructureUtils.checkDataStructureSize(size);
        if (firstList == null) {
            firstList = new List<>();
            firstList.data = element;
            lastList = firstList;
        } else {
            List<T> newList = new List<>();
            newList.data = element;
            newList.previous = lastList;
            lastList.next = newList;
            lastList = newList;
        }
        size++;
    }

    /**
     * Adding element to g linked list strictly
     *
     * @param element element for adding
     */
    @Override
    public void strictAdd(T element) {
        StructureUtils.checkDataStructureSize(size);
        if (firstList == null) {
            firstList = new List<>();
            firstList.data = element;
            lastList = firstList;
        } else {
            List<T> newList = new List<>();
            newList.data = (T) SystemUtils.clone(element);
            newList.previous = lastList;
            lastList.next = newList;
            lastList = newList;
        }
        size++;
    }

    /**
     * Replace element on given index
     *
     * @param index   index for replacing
     * @param element element for replacing
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
     * Replace element on given index strictly
     *
     * @param index   index for replacing
     * @param element element for replacing
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
     * Get element by index
     *
     * @param index index of element for getting
     * @return element by index
     * @throws IncorrectIndexException if @index out of bounds
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
        if (size == 0) {
            throw new IncorrectIndexException("List is empty");
        } else {
            return lastList.data;
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
     * Removing element with index = 0
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
        if (size == 0) {
            throw new IncorrectIndexException("List is empty");
        } else if (size == 1) {
            size--;
            firstList = null;
        } else {
            lastList = lastList.previous;
            lastList.next = null;
            size--;
        }
    }

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
            for (int i = 0; i < index; i++) {
                tempList = tempList.next;
            }
            tempList.previous.next = tempList.next;
            if (tempList.next != null) {
                tempList.next.previous = tempList.previous;
            }
            tempList = null; //escape obsolete references
            size--;
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
     * @return iterator of g linked list
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
        if (o == null || !(o instanceof GLinkedList)) return false;
        GLinkedList<T> list1 = (GLinkedList<T>) o;
        if (size != list1.size) return false;

        List<T> tempList = firstList;
        List<T> tempList1 = list1.firstList;
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
                List<T> tempList = firstList;
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
        List<T> tempList = firstList;
        while (tempList != null) {
            result.append(tempList.data);
            result.append(", ");
            tempList = tempList.next;
        }
        if (result.length() > 1) {
            result.delete(result.length() - 2, result.length());
        }
        return "DoublyLinkedList{" + result.toString() + "}";
    }
}
