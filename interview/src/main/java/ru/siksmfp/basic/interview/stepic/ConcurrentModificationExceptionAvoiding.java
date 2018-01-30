package com.saiu.algorithms.interview.stepic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Artem Karnov @date 04.05.17.
 *         artem.karnov@t-systems.com
 */

public class ConcurrentModificationExceptionAvoiding {
    public static void main(String[] args) {

        List<Integer> delList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            delList.add(i);
        }

        Iterator iterator = delList.iterator();
        while (iterator.hasNext()) {
            if ((Integer) iterator.next() < 10) {
                iterator.remove();
            }
        }

        //don't do it

        //When we iterate and modify collection
        //throws ConcurrentModificationException

        /*

          for (Integer num : delList) {
            if (num < 10) delList.remove(num);
        }

        */

        System.out.println(delList);

    }
}
