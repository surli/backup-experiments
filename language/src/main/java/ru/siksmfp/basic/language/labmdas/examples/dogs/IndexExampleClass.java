package ru.siksmfp.basic.language.labmdas.examples.dogs;

/**
 * @author Artem Karnov @date 03.05.2017.
 *         artem.karnov@t-systems.com
 */

public class IndexExampleClass<T> {
    private int index;
    private T value;

    public IndexExampleClass(int index, T value) {
        this.index = index;
        this.value = value;
    }
}
