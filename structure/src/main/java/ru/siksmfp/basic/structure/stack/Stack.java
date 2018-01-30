package ru.siksmfp.basic.structure.stack;

import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.array.dynamic.DynamicArray;
import ru.siksmfp.basic.structure.array.g.GArray;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;
import ru.siksmfp.basic.structure.utils.StructureUtils;
import ru.siksmfp.basic.structure.utils.SystemUtils;

/**
 * Created by Artyom Karnov on 18.11.16.
 * artyom-karnov@yandex.ru
 * <p>
 * Stack built on dynamic array
 *
 * @param <T> object type for storing in stack
 */
// TODO: 1/5/2018 Cross stucture sortable
public class Stack<T> {
    private final ArrayStructure<T> array;
    private int size;
    private static final int INITIAL_STACK_SIZE = 1000;

    /**
     * Stack initialization
     */
    public Stack() {
        size = 0;
        array = new GArray<>(INITIAL_STACK_SIZE);
    }

    public Stack(int size) {
        this.size = 0;
        array = new GArray<>(size);
    }

    /**
     * Pushing element stack
     *
     * @param element element for pushing
     */
    public void push(T element) {
        StructureUtils.checkDataStructureSize(size);
        array.add(size, element);
        size++;
    }

    /**
     * Strict pushing element stack
     *
     * @param element element for pushing
     */
    public void strictPush(T element) {
        StructureUtils.checkDataStructureSize(size);
        array.add((T) SystemUtils.clone(element));
        size++;
    }

    /**
     * Getting first stack element without deleting it
     *
     * @return first stack element
     */
    public T peek() {
        if (size > 0) {
            return array.get(size - 1);
        } else {
            throw new IncorrectSizeException("Stack is empty");
        }
    }

    /**
     * Extraction upper element from stack
     *
     * @return extracted element
     */
    public T pop() {
        if (size > 0) {
            T temp = array.get(size - 1);
            array.add(size - 1, null);
            array.remove(size - 1);
            size--;
            return temp;
        } else {
            throw new IncorrectSizeException("Stack is empty");
        }
    }

    /**
     * Checking stack on elements presence
     *
     * @return true - if stack has elements, false - if stack is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Getting stack size
     *
     * @return size of array
     */
    public int size() {
        return size;
    }

    /**
     * Checking elements storing in array
     *
     * @param element element for checking
     * @return true - if stack contain adjusted element, false - if doesn't
     */
    public boolean contains(T element) {
        return array.contains(element);
    }
}