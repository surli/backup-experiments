package ru.siksmfp.basic.structure.api;

import java.util.function.Function;

public interface HashTable<K, V> {
    void add(K key, V value);

    int getHash(K key);

    V get(K key);

    void setHashFunction(Function<K, Integer> hashFunction);

    void remove(K key);

    void delete(K key);

    int size();
}
