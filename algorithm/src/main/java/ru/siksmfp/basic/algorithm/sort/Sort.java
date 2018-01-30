package ru.siksmfp.basic.algorithm.sort;

import ru.siksmfp.basic.structure.api.ArrayStructure;

/**
 * @author Artem Karnov @date 21.04.2017.
 * artem.karnov@t-systems.com
 */
public class Sort {

    private Sort() {
    }

    /**
     * Classic bubble sort with default ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    public static <T extends Comparable<T>> void bubbleSort(ArrayStructure<T> structure) {
        bubbleAscSort(structure);
    }

    /**
     * Classic bubble sorting
     *
     * @param structure     structure for sorting
     * @param sortDirection sort direction
     * @param <T>           type of sort elements
     */
    public static <T extends Comparable<T>> void bubbleSort(ArrayStructure<T> structure, SortDirection sortDirection) {
        if (sortDirection == SortDirection.ASC) {
            bubbleAscSort(structure);
        } else {
            bubbleDescSort(structure);
        }
    }

    /**
     * Classic select sort with default ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    public static <T extends Comparable<T>> void selectionSort(ArrayStructure<T> structure) {
        selectionAscSort(structure);
    }

    /**
     * Classic selection sorting
     *
     * @param structure     structure for sorting
     * @param sortDirection sort direction
     * @param <T>           type of sort elements
     */
    public static <T extends Comparable<T>> void selectionSort(ArrayStructure<T> structure, SortDirection sortDirection) {
        if (sortDirection == SortDirection.ASC) {
            selectionAscSort(structure);
        } else {
            selectionDescSort(structure);
        }
    }

    /**
     * Insertion sorting with default ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    public static <T extends Comparable<T>> void insertionSort(ArrayStructure<T> structure) {
        insertionAscSort(structure);
    }

    /**
     * Insertion  sorting
     *
     * @param structure     structure for sorting
     * @param sortDirection sort direction
     * @param <T>           type of sort elements
     */
    public static <T extends Comparable<T>> void insertionSort(ArrayStructure<T> structure, SortDirection sortDirection) {
        if (sortDirection == SortDirection.ASC) {
            insertionAscSort(structure);
        } else {
            insertionDescSort(structure);
        }
    }

    /**
     * Classic insertion sorting with default ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    public static <T extends Comparable<T>> void classicInsertionSort(ArrayStructure<T> structure) {
        classicInsertionAscSort(structure);
    }

    /**
     * Classic insertion sorting
     *
     * @param structure     structure for sorting
     * @param sortDirection sort direction
     * @param <T>           type of sort elements
     */
    public static <T extends Comparable<T>> void classicInsertionSort(ArrayStructure<T> structure, SortDirection sortDirection) {
        if (sortDirection == SortDirection.ASC) {
            classicInsertionAscSort(structure);
        } else {
            classicInsertionDescSort(structure);
        }
    }

    /**
     * Bubble sorting with ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void bubbleDescSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            for (int j = i; j < structure.size(); j++) {
                if (structure.get(i).compareTo(structure.get(j)) < 0) {
                    swap(structure, i, j);

                }
            }
        }
    }	

    /**
     * Bubble sorting with DESCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void bubbleAscSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            for (int j = i; j < structure.size(); j++) {
                if (structure.get(i).compareTo(structure.get(j)) > 0) {
                    swap(structure, i, j);
                }
            }
        }
    }

    /**
     * Selection sorting with ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void selectionAscSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            int localIndexMin = i;
            for (int j = i; j < structure.size(); j++) {
                if (structure.get(j).compareTo(structure.get(localIndexMin)) < 0) {
                    localIndexMin = j;
                }
            }
            swap(structure, localIndexMin, i);
        }
    }

    /**
     * Selection sorting with DESCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void selectionDescSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            int localIndexMax = i;
            for (int j = i; j < structure.size(); j++) {
                if (structure.get(j).compareTo(structure.get(localIndexMax)) > 0) {
                    localIndexMax = j;
                }
            }
            swap(structure, localIndexMax, i);
        }
    }

    /**
     * Insertion sorting with ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void insertionAscSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            int leftIndex = getLeftBorderWithElementMoreThan(structure, i);
            rightShift(structure, leftIndex, i);
        }
    }

    /**
     * Insertion sorting with DESCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void insertionDescSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            int leftIndex = getLeftBorderWithElementLessThan(structure, i);
            rightShift(structure, leftIndex, i);
        }
    }

    /**
     * Calculate left border for ASC sorting
     *
     * @param structure  structure for searching
     * @param rightIndex first index for searching (last is 0)
     * @param <T>        type of sort elements
     * @return index of element that less than element with rightIndex
     */
    private static <T extends Comparable<T>> int getLeftBorderWithElementMoreThan(ArrayStructure<T> structure, int rightIndex) {
        for (int i = rightIndex - 1; i >= 0; i--) {
            if (structure.get(rightIndex).compareTo(structure.get(i)) > 0)
                return i + 1;
        }
        return 0;
    }

    /**
     * Calculate left border for DESC sorting
     *
     * @param structure  structure for searching
     * @param rightIndex first index for searching (last is 0)
     * @param <T>        type of sort elements
     * @return index of element that more than element with rightIndex
     */
    private static <T extends Comparable<T>> int getLeftBorderWithElementLessThan(ArrayStructure<T> structure, int rightIndex) {
        for (int i = rightIndex - 1; i >= 0; i--) {
            if (structure.get(rightIndex).compareTo(structure.get(i)) < 0)
                return i + 1;
        }
        return 0;
    }

    /**
     * Circular right elements shifting
     * Consider [5] [4] [3] [2] [1]
     * Result   [1] [5] [4] [3] [2]
     *
     * @param structure  structure for shifting
     * @param leftIndex  initial element's index
     * @param rightIndex end element's index
     * @param <T>        type of sort elements
     */
    private static <T extends Comparable<T>> void rightShift(ArrayStructure<T> structure, int leftIndex, int rightIndex) {
        T localMinValue = structure.get(rightIndex);
        for (int i = rightIndex; i > leftIndex; i--) {
            structure.add(i, structure.get(i - 1));
        }
        structure.add(leftIndex, localMinValue);
    }

    /**
     * Classic element swapping
     *
     * @structure structure for swapping
     * @index1 e1 first element
     * @index2 e2 second element
     */
    private static <T extends Comparable<T>> void swap(ArrayStructure<T> structure, int index1, int index2) {
        if (index1 != index2) {
            T temp = structure.get(index1);
            structure.add(index1, structure.get(index2));
            structure.add(index2, temp);
        }
    }

    /**
     * Classic insertion sorting with ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void classicInsertionAscSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            int j = i;
            while (j > 0) {
                if (structure.get(j).compareTo(structure.get(j - 1)) < 0) {
                    swap(structure, j, j - 1);
                }
                j--;
            }
        }
    }

    /**
     * Classic insertion sorting with ASCENDING ordering
     *
     * @param structure structure for sorting
     * @param <T>       type of sort elements
     */
    private static <T extends Comparable<T>> void classicInsertionDescSort(ArrayStructure<T> structure) {
        for (int i = 0; i < structure.size(); i++) {
            int j = i;
            while (j > 0) {
                if (structure.get(j).compareTo(structure.get(j - 1)) > 0) {
                    swap(structure, j, j - 1);
                }
                j--;
            }
        }
    }
}

