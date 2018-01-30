package ru.siksmfp.basic.structure.hash.table.binary;

import ru.siksmfp.basic.structure.api.ArrayStructure;
import ru.siksmfp.basic.structure.api.HashTable;
import ru.siksmfp.basic.structure.array.fixed.FixedArray;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;
import ru.siksmfp.basic.structure.utils.math.Math;

import java.util.function.Function;

/**
 * @author Artem Karnov @date 1/30/2018.
 * @email artem.karnov@t-systems.com
 * <p>
 * Hash table with binary probing
 */
public class BinaryProbHashTable<K, V> implements HashTable<K, V> {
    /**
     * Structure for storing key-value structure
     *
     * @param <K> - key of structure
     * @param <V> - value of structure
     */
    private class Node<K, V> {
        private K key;
        private V value;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node<K, V> node = (Node<K, V>) o;
            return key.equals(node.key) &&
                    value.equals(node.value);
        }

        @Override
        public int hashCode() {
            return 31 * key.hashCode() + 31 * value.hashCode();
        }
    }

    /**
     * Default hash function is being applying whe custom function isn't given
     */
    private final Function<K, Integer> DEFAULT_HASH_FUNCTION = t -> {
        int i = t.hashCode();
        i = (i + 0x7ed55d16) + (i << 12);
        i = (i ^ 0xc761c23c) ^ (i >> 19);
        i = (i + 0x165667b1) + (i << 5);
        i = (i + 0xd3a2646c) ^ (i << 9);
        i = (i + 0xfd7046c5) + (i << 3);
        i = (i ^ 0xb55a4f09) ^ (i >> 16);
        return i > 0 ? i : -i;
    };
    private ArrayStructure<Node<K, V>> array;
    private Function<K, Integer> hashFunction;
    private int size;

    /**
     * Constructor with size initialization
     *
     * @param size size of table
     */
    public BinaryProbHashTable(int size) {
        array = new FixedArray<>(Math.getFirstSimpleNumberAfter(size));
        hashFunction = DEFAULT_HASH_FUNCTION;
    }

    /**
     * Add key - value structure to hash table
     *
     * @param key   key for adding
     * @param value value for adding
     */
    @Override
    public void add(K key, V value) {
        int index = applyHashing(key);
        if (array.get(index) == null) {
            addNoteToArray(index, new Node<>(key, value));
        } else {
            int i = 2;
            while (i < array.size()) {
                if (index == 0 || index == 1) {
                    index = 2;
                } else {
                    index = (int) Math.pow(index, 2);
                }

                if (index > array.size()) {
                    index = i;
                    i++;
                }

                if (array.get(index) == null) {
                    addNoteToArray(i, new Node<>(key, value));
                    return;
                }
            }
            throw new IncorrectSizeException("There is no space for new element");
        }
    }

    /**
     * Key hashing with given hash function
     *
     * @param key key for hashing
     * @return result of applying hash function on key
     */
    @Override
    public int getHash(K key) {
        return hashFunction.apply(key);
    }

    /**
     * Getting value by key from hash table
     *
     * @param key key for getting
     * @return stored value found by key
     */
    @Override
    public V get(K key) {
        int index = applyHashing(key);
        if (array.get(index) != null && array.get(index).key.equals(key)) {
            return array.get(index).value;
        } else {
            int i = 0;
            while (i < array.size()) {
                if (index == 0 || index == 1) {
                    index++;
                } else {
                    index = (int) Math.pow(index, 2);
                }

                if (index > array.size()) {
                    index = i;
                    i++;
                }

                if (array.get(index) != null && array.get(index).key.equals(key)) {
                    return array.get(index).value;
                }
            }
            return null;
        }
    }

    /**
     * Setting hash function for hash calculation
     * <p>
     * Be careful setting function after using initialized object of
     * hash table. You can loose your data.
     * Typical use case is set function before hash table using
     *
     * @param hashFunction hash function implementation
     */
    @Override
    public void setHashFunction(Function<K, Integer> hashFunction) {
        this.hashFunction = hashFunction;
    }

    /**
     * Remove key - value structure by key
     *
     * @param key key for deleting
     */
    @Override
    public void remove(K key) {
        int index = applyHashing(key);
        if (array.get(index) != null && array.get(index).key.equals(key)) {
            array.add(index, null);
            size--;
        } else {
            int i = index + 1;
            while (i != index) {
                if (i >= array.size()) {
                    i = 0;
                } else if (array.get(i) != null && array.get(i).key.equals(key)) {
                    array.add(i, null);
                    size--;
                    return;
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Replacing value of key - value structure on NULL by key
     *
     * @param key key for replacing
     */
    @Override
    public void delete(K key) {
        int index = applyHashing(key);
        if (array.get(index).key.equals(key)) {
            array.get(index).value = null;
        } else {
            int i = index + 1;
            while (i != index) {
                if (i >= array.size()) {
                    i = 0;
                } else if (array.get(i) != null && array.get(i).key.equals(key)) {
                    array.get(i).value = null;
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Getting number of adding elements
     *
     * @return number of stored elements of hash table
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ru.siksmfp.basic.structure.hash.table.open.adress.linear.prob.LinearProbHashTable))
            return false;
        BinaryProbHashTable<K, V> table1 = (BinaryProbHashTable<K, V>) o;
        if (size != table1.size) return false;

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) == null && table1.array.get(i) != null) {
                return false;
            }

            if (array.get(i) != null && table1.array.get(i) == null) {
                return false;
            }

            if (array.get(i) != null && table1.array.get(i) != null)
                if (!array.get(i).equals(table1.array.get(i))) {
                    return false;
                }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * size;
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) != null) {
                result += 31 * array.get(i).value.hashCode();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) != null) {
                result.append("(")
                        .append(array.get(i).key)
                        .append(";")
                        .append(array.get(i).value)
                        .append("), ");
            } else {
                result.append("null, ");
            }
        }
        if (result.length() > 1) {
            result.delete(result.length() - 2, result.length());
        }
        return "BinaryProbHashTable{" + result.toString() + "}";
    }


    /**
     * Adding note to allocated
     *
     * @param index index for adding
     * @param node  node for adding
     */
    private void addNoteToArray(int index, Node<K, V> node) {
        array.add(index, node);
        size++;
    }

    /**
     * Calculating index of structure in allocated array
     *
     * @param key key for applying
     * @return index of key in allocated array
     */
    private int applyHashing(K key) {
        return hashFunction.apply(key) % array.size();
    }
}
