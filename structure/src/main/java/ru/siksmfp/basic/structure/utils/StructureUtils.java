package ru.siksmfp.basic.structure.utils;

import ru.siksmfp.basic.structure.exceptions.IncorrectIndexException;
import ru.siksmfp.basic.structure.exceptions.IncorrectSizeException;

/**
 * @author Artem Karnov @date 1/3/2018.
 * artem.karnov@t-systems.com
 */
public class StructureUtils {
    /**
     * Method checks structure size is in allowed range
     *
     * @param size size of structure
     * @throws IncorrectSizeException if size more than Integer.MAX_VALUE or less than 0
     */
    public static void checkDataStructureSize(int size) {
        if (size == Integer.MAX_VALUE) {
            throw new IncorrectSizeException("Size of structure is too large");
        } else if (size < 0) {
            throw new IncorrectSizeException("Size can't be less than 0");
        }
    }

    /**
     * Checking correctness of index
     *
     * @param index index for checking
     * @param size  size of structure
     * @throws IncorrectIndexException if index more than size and less thn 0
     */
    public static void checkingIndex(int index, int size) {
        if (index < 0) {
            throw new IncorrectIndexException("Index couldn't be less than 0");
        } else if (index >= size) {
            throw new IncorrectIndexException("Index couldn't be more than size-1");
        }
    }
}
