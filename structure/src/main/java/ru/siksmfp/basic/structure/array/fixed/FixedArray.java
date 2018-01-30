package ru.siksmfp.basic.structure.array.fixed;

import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.api.Iterator;
import ru.siksmfp.basic.structure.api.ListStructure;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;
import ru.siksmfp.basic.structure.utils.StructureUtils;
import ru.siksmfp.basic.structure.utils.SystemUtils;

/**
 * Created by Artyom Karnov on 15.11.16.
 * artyom-karnov@yandex.ru
 * <p>
 * DynamicArray of fix length.
 *
 * @param <T> object type for storing in array
 */
public class FixedArray<T> implements ArrayStructure<T> {
    private final Object[] array;
    private final int maxSize;

    /**
     * Constructor for element array creation
     *
     * @param elements - elements for string
     */
    public FixedArray(T... elements) {
        maxSize = elements.length;
        array = new Object[maxSize];
        for (int i = 0; i < elements.length; i++) {
            array[i] = SystemUtils.clone(elements[i]);
        }
    }

    /**
     * Creating DynamicArray by copying other array in constructor
     *
     * @param arrayForCopy array for copy
     */
    public FixedArray(ArrayStructure<T> arrayForCopy) {
        maxSize = arrayForCopy.size();
        array = new Object[maxSize];
        for (int i = 0; i < maxSize; i++) {
            array[i] = SystemUtils.clone(arrayForCopy.get(i));
        }
    }

    /**
     * Creating dynamic array from list
     *
     * @param listStructure - list for creation
     */
    public FixedArray(ListStructure<T> listStructure) {
        maxSize = listStructure.size();
        array = new Object[maxSize];
        Iterator iterator = listStructure.getIterator();
        for (int i = 0; iterator.hasNext(); i++) {
            array[i] = SystemUtils.clone(iterator.next());
        }
    }

    /**
     * Constructor for getting array's size and initialization
     *
     * @param size array size
     */
    public FixedArray(int size) {
        StructureUtils.checkDataStructureSize(size);
        this.maxSize = size;
        array = new Object[size];
    }

    /**
     * Getting element on adjusted index in array
     *
     * @param index index of element
     * @return element on adjusted index
     */
    public T get(int index) {
        StructureUtils.checkingIndex(index, size());
        return (T) array[index];
    }

    /**
     * Addition new element on specified index
     *
     * @param element element for adding
     * @param index   index of adding
     */
    public void add(int index, T element) {
        StructureUtils.checkingIndex(index, size());
        array[index] = element;
    }

    /**
     * Add element on first vacant place from beginning
     * (using deep cloning during array shifting)
     * <p>
     * If no vacant place @throws IncorrectSizeException
     * Example [1] [2] [] [] []
     * Result [1] [2] [NEW] [] []
     *
     * @param element element for adding
     */
    @Override
    public void add(T element) {
        for (int i = 0; i < size(); i++) {
            if (array[i] == null) {
                array[i] = element;
                return;
            }
        }
        throw new IncorrectSizeException("There is no vacant place for new element");
    }

    /**
     * Addition new element on specified index strictly
     * (using deep cloning during addition)
     *
     * @param index   index for adding
     * @param element element for adding
     */
    @Override
    public void strictAdd(int index, T element) {
        StructureUtils.checkingIndex(index, size());
        array[index] = SystemUtils.clone(element);
    }

    /**
     * Removing element on adjusted index
     *
     * @param index element's index in array
     */
    public void remove(int index) {
        StructureUtils.checkingIndex(index, size());
        leftShift(index);
    }

    /**
     * Add element on first vacant place from beginning strictly
     * (using deep cloning during array shifting)
     * <p>
     * If no vacant place @throws IncorrectSizeException
     * Example [1] [2] [] [] []
     * Result [1] [2] [NEW] [] []
     *
     * @param element element for adding
     */
    @Override
    public void strictAdd(T element) {
        for (int i = 0; i < size(); i++) {
            if (array[i] == null) {
                array[i] = SystemUtils.clone(element);
                return;
            }
        }
        throw new IncorrectSizeException("There is no vacant place for new element");
    }

    /**
     * Strict removing element on given index
     * Difference from @remove method in implemented deep copying during element's offset
     *
     * @param index index of removing element
     */
    @Override
    public void strictRemove(int index) {
        StructureUtils.checkingIndex(index, size());
        strictLeftShifting(index);
    }

    /**
     * Deleting element on given index form array
     * NULL-value is being set instead element on given index
     *
     * @param index index of deleting element
     */
    @Override
    public void delete(int index) {
        StructureUtils.checkingIndex(index, size());
        array[index] = null;
    }

    /**
     * Getting array size
     *
     * @return size of array
     */
    public int size() {
        return maxSize;
    }

    /**
     * Checking elements storing in array
     *
     * @param element element for checking
     * @return true - if array contain adjusted element, false - if doesn't
     */
    public boolean contains(T element) {
        for (int i = 0; i < maxSize; i++) {
            if (array[i].equals(element))
                return true;
        }
        return false;
    }

    /**
     * Checking element's existing in array
     *
     * @return true if size=0, false if size>0
     */
    public boolean isEmpty() {
        return maxSize == 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixedArray<T> array1 = (FixedArray<T>) o;
        if (maxSize != array1.maxSize) return false;

        for (int i = 0; i < maxSize; i++) {
            if (array[i] == null || array1.array[i] == null) {
                if (array[i] == null && array1.array[i] != null)
                    return false;
                if (array[i] != null && array1.array[i] == null)
                    return false;
            } else {
                if (!array[i].equals(array1.array[i]))
                    return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * maxSize;
        for (int i = 0; i < maxSize; i++) {
            if (array[i] != null) {
                result += 31 * array[i].hashCode();
            } else {
                result += 31;
            }
        }
        return result;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxSize; i++) {
            result.append(array[i]);
            result.append(", ");
        }
        if (result.length() > 1) {
            result.delete(result.length() - 2, result.length());
        }
        return "DynamicArray{" + result.toString() + "}";
    }

    /**
     * DynamicArray left shifting
     * <p>
     * Consider [1] [2] [3] [4]. We have to shift array since index = 1
     * Then we have [1] [3] [4] [NULL]
     *
     * @param startWithIndex first index for shifting
     */
    private void leftShift(int startWithIndex) {
        for (int i = startWithIndex; i < size() - 1; i++) {
            array[i] = array[i + 1];
        }
        array[size() - 1] = null;
    }

    /**
     * Strict array left shifting
     * <p>
     * Consider [1] [2] [3] [4]. We have to shift array since index = 1
     * Then we have [1] [3] [4] [NULL]
     *
     * @param startWithIndex first index for shifting
     */
    private void strictLeftShifting(int startWithIndex) {
        for (int i = startWithIndex; i < size() - 1; i++) {
            array[i] = SystemUtils.clone(array[i + 1]);
        }
        array[size() - 1] = null;
    }
}