package ru.siksmfp.basic.structure.deck;

import ru.siksmfp.basic.structure.array.dynamic.DynamicArray;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;

/**
 * @author Artem Karnov @date 02.12.16.
 * artem.karnov@t-systems.com
 *
 * @param <T> object type for storing in deck
 * <p>
 * Deck built on dynamic array
 */
public class Deck<T> {
    private DynamicArray<T> array;
    private int size;

    /**
     * Deck initialization
     */
    public Deck() {
        size = 0;
        array = new DynamicArray<T>();
    }

    /**
     * Pushing element into begin of deck
     *
     * @param element element for pushing
     */
    public void pushIntoEnd(T element) {
        if (size < Integer.MAX_VALUE) {
            array.add(element);
            size++;
        } else {
            throw new IncorrectSizeException("Size is too big for storing");
        }
    }

    /**
     * Pushing element into end of deck
     *
     * @param element element for pushing
     */
    public void pushIntoBegin(T element) {
        if (size < Integer.MAX_VALUE) {
            DynamicArray<T> newArray = new DynamicArray<T>();
            newArray.add(element);
            for (int i = 0; i < size; i++) {
                newArray.add(array.get(i));
            }
            array = newArray;
            size++;
        } else {
            throw new IncorrectSizeException("Size is too big for storing");
        }
    }

    /**
     * Extraction first element from deck
     *
     * @return extracted element
     */
    public T popBegin() {
        if (size > 0) {
            T element = array.get(0);
            array.remove(0);
            size--;
            return element;
        } else {
            throw new IncorrectSizeException("Deck is empty");
        }
    }

    /**
     * Extraction last element from deck
     *
     * @return extracted element
     */
    public T popBack() {
        if (size > 0) {
            T element = array.get(size - 1);
            array.remove(size - 1);
            size--;
            return element;
        } else {
            throw new IncorrectSizeException("Deck is empty");
        }
    }

    /**
     * Checking com.saiu.ru.siksmfp.basic.structure.deck on elements presence
     *
     * @return true - if deck has elements, false - if deck is empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Getting deck size
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
     * @return true - if deck contain adjusted element, false - if doesn't
     */
    public boolean contain(T element) {
        return array.contains(element) ? true : false;
    }
}