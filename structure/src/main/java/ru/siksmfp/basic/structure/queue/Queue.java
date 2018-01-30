package ru.siksmfp.basic.structure.queue;


import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.array.dynamic.DynamicArray;
import ru.siksmfp.basic.structure.array.g.GArray;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;
import ru.siksmfp.basic.structure.utils.StructureUtils;
import ru.siksmfp.basic.structure.utils.SystemUtils;

/**
 * @author Artem Karnov @date 21.02.17.
 * artem.karnov@t-systems.com
 * <p>
 * Queue built on dynamic array
 * <p>
 * *@param <T> object type for storing in queue
 */
public class Queue<T> {
    private ArrayStructure<T> array;
    private int size;
    private static final int INITIAL_QUEUE_SIZE = 1000;

    /**
     * Queue initialization
     */
    public Queue() {
        size = 0;
        array = new GArray<>();
    }

    public Queue(int size) {
        this.size = size;
        array = new GArray<>(size);
    }

    /**
     * Pushing element into queue
     *
     * @param element element for pushing
     */
    public void push(T element) {
        StructureUtils.checkDataStructureSize(size);
        array.add(element);
        size++;
    }

    /**
     * Strict pushing element into queue
     *
     * @param element element for pushing
     */
    public void strictPush(T element) {
        StructureUtils.checkDataStructureSize(size);
        array.add((T) SystemUtils.clone(element));
        size++;
    }

    /**
     * Getting first element in queue without deleting it
     *
     * @return first element in queue
     */
    public T peek() {
        if (size > 0) {
            return array.get(0);
        } else {
            throw new IncorrectSizeException("Queue is empty");
        }
    }

    /**
     * Extraction first element from queue
     *
     * @return extracted element
     */
    public T pop() {
        if (size > 0) {
            T element = array.get(0);
            array.remove(0);
            size--;
            return element;
        } else {
            throw new IncorrectSizeException("Queue is empty");
        }
    }

    /**
     * Checking queue on elements presence
     *
     * @return true - if queue has elements, false - if queue is empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Getting queue size
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
     * @return true - if queue contain adjusted element, false - if doesn't
     */
    public boolean contains(T element) {
        return array.contains(element);
    }
}