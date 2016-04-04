package com.toddway.shelf;

/**
 * Created by tway on 4/4/16.
 */
public class ShelfUtils {

    static boolean hasRxOnClasspath() {
        try {
            Class.forName("rx.Observable");
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ShelfUtils() {
        //no instance
    }

    public static void checkRx() {
        if (!hasRxOnClasspath()) {
            throw new NoClassDefFoundError("RxJava is not on classpath, add it to your dependencies");
        }
    }

    static boolean hasGsonOnClasspath() {
        try {
            Class.forName("com.google.gson.Gson");
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void checkGson() {
        if (!hasGsonOnClasspath()) {
            throw new NoClassDefFoundError("Gson is not on classpath, add it to your dependencies");
        }
    }
}
