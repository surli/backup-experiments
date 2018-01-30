package com.saiu.algorithms.interview.stepic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * @author Artem Karnov @date 02.05.2017.
 *         artem.karnov@t-systems.com
 */

public class ListIterating {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> list = Arrays.stream(reader.readLine().split("\\s")).collect(Collectors.toList());

        ListIterator<String> iter = list.listIterator();
        while (iter.hasNext()) {
            String currentString = iter.next();
            System.out.println(currentString);
            if (currentString.equals("Hip")) {
                System.out.println("Hop");
            }
        }

        //or

        ListIterator<String> listIterator = list.listIterator();
        while (listIterator.hasNext()){
            if (listIterator.next().equals("Hip"))
                listIterator.add("Hop");
        }
        list.listIterator().forEachRemaining(System.out::println);
    }


}
