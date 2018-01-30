package ru.siksmfp.basic.structure.array.dynamic;

import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.api.Iterator;
import ru.siksmfp.basic.structure.api.ListStructure;
import ru.siksmfp.basic.structure.utils.StructureUtils;
import ru.siksmfp.basic.structure.utils.SystemUtils;

/**
 * Created by Artyom Karnov on 17.11.16.
 * artyom-karnov@yandex.ru
 * <p>
 * DynamicArray of variable length.
 *
 * @param <T> object type for storing in array
 */
public class DynamicArray<T> implements ArrayStructure<T> {
    private Object[] array;
    private int size;

    /**
     * Constructor without parameters for zero-length array creation
     */
    public DynamicArray() {
        array = new Object[0];
        size = 0;
    }

    /**
     * Constructor for element array creation
     *
     * @param elements - elements for string
     */
    public DynamicArray(T... elements) {
        size = elements.length;
        array = new Object[size];
        for (int i = 0; i < elements.length; i++) {
            array[i] = SystemUtils.clone(elements[i]);
        }
    }

    /**
     * Creating DynamicArray by copying other array in constructor
     *
     * @param arrayForCopy array for copy
     */
    public DynamicArray(ArrayStructure<T> arrayForCopy) {
        size = arrayForCopy.size();
        array = new Object[size];
        for (int i = 0; i < size; i++) {
            array[i] = SystemUtils.clone(arrayForCopy.get(i));
        }
    }

    /**
     * Creating dynamic array from list
     *
     * @param listStructure - list for creation
     */
    public DynamicArray(ListStructure<T> listStructure) {
        size = listStructure.size();
        array = new Object[size];
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
    public DynamicArray(int size) {
        StructureUtils.checkDataStructureSize(size);
        this.size = size;
        array = new Object[size];
    }

    /**
     * Add element on given index
     *
     * @param element element for adding
     * @param index   index of adding element
     */
    @Override
    public void add(int index, T element) {
        StructureUtils.checkingIndex(index, size);
        array[index] = element;
    }

    /**
     * Add element on given position by deep copying of element
     *
     * @param element element for adding
     * @param index   position of adding element
     */
    @Override
    public void strictAdd(int index, T element) {
        StructureUtils.checkingIndex(index, size);
        array[index] = SystemUtils.clone(element);
    }

    /**
     * Add element to array
     *
     * @param element element for adding
     */
    public void add(T element) {
        Object[] extendedArray = new Object[size + 1];
        for (int i = 0; i < size; i++) {
            extendedArray[i] = array[i];
        }
        extendedArray[size] = element;
        array = extendedArray;
        size++;
    }

    /**
     * Add element to array strictly. Using deep cloning
     *
     * @param element element for adding
     */
    @Override
    public void strictAdd(T element) {
        Object[] extendedArray = new Object[size + 1];
        for (int i = 0; i < size; i++) {
            extendedArray[i] = SystemUtils.clone(array[i]);
        }
        extendedArray[size] = SystemUtils.clone(element);
        array = extendedArray;
        size++;
    }

    /**
     * Getting element on adjusted index in array
     *
     * @param index index of element
     * @return element on adjusted index
     */
    @Override
    public T get(int index) {
        StructureUtils.checkingIndex(index, size);
        return (T) array[index];
    }

    /**
     * Removing element on adjusted index
     *
     * @param index element's index in array
     */
    @Override
    public void remove(int index) {
        StructureUtils.checkingIndex(index, size());
        leftShift(index);
        size--;
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
        strictLeftShift(index);
        size--;
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
    @Override
    public int size() {
        return size;
    }

    /**
     * Checking elements storing in array
     *
     * @param element element for checking
     * @return true - if array contain adjusted element, false - if doesn't
     */
    @Override
    public boolean contains(T element) {
        for (int i = 0; i < size; i++) {
            if (array[i] != null) {
                if (array[i].equals(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checking element's existing in array
     *
     * @return true if size=0, false if size>0
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicArray<T> array1 = (DynamicArray<T>) o;
        if (size != array1.size) return false;

        for (int i = 0; i < size; i++) {
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
        int result = 31 * size;
        for (int i = 0; i < size; i++) {
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
        for (int i = 0; i < size; i++) {
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
     * DynamicArray left shifting
     * <p>
     * Consider [1] [2] [3] [4]. We have to shift array since index = 1
     * Then we have [1] [3] [4] [NULL]
     * <p>
     * Strict means for copying we use deep clone mechanism
     *
     * @param startWithIndex first index for shifting
     */
    private void strictLeftShift(int startWithIndex) {
        for (int i = startWithIndex; i < size() - 1; i++) {
            array[i] = SystemUtils.clone(array[i + 1]);
        }
        array[size() - 1] = null;
    }
}