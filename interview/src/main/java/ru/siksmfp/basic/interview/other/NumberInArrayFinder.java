package ru.siksmfp.basic.interview.other;


/**
 * @author Artem Karnov @date 25.02.17.
 * artem.karnov@t-systems.com
 */

import ru.siksmfp.basic.structure.array.fixed.FixedArray;

/**
 * Find one of the numbers which exists in each of three nondecreasing arrays x[p], y[q], z[r].
 * Algorithm  complexity should be O(p+q+r).
 *
 * @param <T>
 */
public class NumberInArrayFinder<T> {
    private int firstIndex = 0;
    private int secondIndex = 0;
    private int thirdIndex = 0;
    private int localMax = 0;
    private int result = 0;

    /**
     * Recursive method for number finding
     * Test #3 is failed
     * Current algorithm can't correctly work with equals number sequences
     * like 8 8 8 8 9 and so on.
     * todo fix it
     *
     * @param firstArray  first array
     * @param secondArray second array
     * @param thirdArray  third arra
     * @return 0 if number doesn't exist, number which exists in each of arrays
     * todo instead of returning 0 should be exception
     * @Algorithm Compare elements in 0-s position.
     * Check their equality
     * if numbers aren't equally we find out maximum of this numbers and move pointers of others
     * to positions with same (max) members. And check equality again.
     */
    public int find(FixedArray<T> firstArray, FixedArray<T> secondArray, FixedArray<T> thirdArray) {
        while (firstIndex < firstArray.size() && secondIndex < secondArray.size()
                && thirdIndex < thirdArray.size()) {

            localMax = getMax((Integer) firstArray.get(firstIndex),
                    (Integer) secondArray.get(secondIndex), (Integer) thirdArray.get(thirdIndex));

            equalize(firstArray, secondArray, thirdArray);

            if (compare((Integer) firstArray.get(firstIndex),
                    (Integer) secondArray.get(secondIndex), (Integer) thirdArray.get(thirdIndex)))
                return (Integer) firstArray.get(firstIndex);
            else {
                if (firstIndex == firstArray.size() - 1 &&
                        secondIndex == secondArray.size() - 1 &&
                        thirdIndex == thirdArray.size() - 1)
                    return 0;
                else {
                    return find(firstArray, secondArray, thirdArray);
                }
            }

        }
        return result;
    }

    /**
     * Getting maximum of 3 numbers
     *
     * @param first  first number
     * @param second second number
     * @param third  third number
     * @return maximum of 3 nmbers
     */
    private int getMax(int first, int second, int third) {
        if (first > second) {
            if (first > third)
                return first;
            else
                return third;
        } else {
            if (second > third)
                return second;
            else
                return third;
        }
    }

    /**
     * Method for pointer correction
     * Each loop-circle we should to control pointer position
     * This method do it.
     *
     * @param firstArray  first array for correction
     * @param secondArray second array for correction
     * @param thirdArray  third array for correction
     */
    private void equalize(FixedArray<T> firstArray, FixedArray<T> secondArray, FixedArray<T> thirdArray) {
        while (localMax > (Integer) firstArray.get(firstIndex) && firstIndex < firstArray.size() - 1) {
            firstIndex++;
        }

        while (localMax > (Integer) secondArray.get(secondIndex) && secondIndex < secondArray.size() - 1) {
            secondIndex++;
        }

        while (localMax > (Integer) thirdArray.get(thirdIndex) && thirdIndex < thirdArray.size() - 1) {
            thirdIndex++;
        }
    }

    /**
     * Comparing 3 numbers for equality
     *
     * @param first  first number
     * @param second second number
     * @param third  third numvber
     * @return true - if all numbers is equally, false - if doesn't
     */
    private boolean compare(int first, int second, int third) {
        return first == second && first == third;
    }


}
