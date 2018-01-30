package com.saiu.algorithms.interview.stepic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListToStringArray {

    public static void main(String[] args) {

        List<String> nameList = new ArrayList<>(Arrays.asList("Mr.Green", "Mr.Yellow", "Mr.Red"));

        String[] strArray = nameList.toArray(new String[nameList.size()]);

        for (int i = 0; i < strArray.length; i++) {
            System.out.println(strArray[i]);
        }

    }
}