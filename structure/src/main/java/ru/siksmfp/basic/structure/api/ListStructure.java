package ru.siksmfp.basic.structure.api;

/**
 * @author Artem Karnov @date 1/7/2018.
 * artem.karnov@t-systems.com
 */
public interface ListStructure<T> {
    void add(T element);

    void strictAdd(T element);

    void replace(int index, T element);

    void strictReplace(int index, T element);

    T get(int index);

    T getFirst();

    T getLast();

    void remove(int index);

    T[] toArray();

    void removeFirst();

    void removeLast();

    void delete(int index);

    int size();

    boolean contains(T element);

    boolean isEmpty();

    Iterator<T> getIterator();
}
