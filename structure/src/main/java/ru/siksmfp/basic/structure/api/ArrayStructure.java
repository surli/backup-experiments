package ru.siksmfp.basic.structure.api;

/**
 * @author Artem Karnov @date 1/4/2018.
 * artem.karnov@t-systems.com
 */
public interface ArrayStructure<T> {
    void add(int index, T element);

    void add(T element);

    void strictAdd(int index, T element);

    void strictAdd(T element);

    T get(int index);

    void remove(int index);

    void strictRemove(int index);

    void delete(int index);

    int size();

    boolean contains(T element);

    boolean isEmpty();
}
