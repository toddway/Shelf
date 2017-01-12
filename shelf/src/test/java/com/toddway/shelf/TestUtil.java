package com.toddway.shelf;

import java.util.List;

/**
 * Created by tway on 1/11/17.
 */

public class TestUtil {

    public static <T> void printValues(List<T> values) {
        for (T value : values) System.out.println("value: " + value);
    }
}
