package com.toddway.shelf;

import com.toddway.shelf.serializer.GsonSerializer;
import com.toddway.shelf.serializer.JavaSerializer;
import com.toddway.shelf.serializer.Serializer;

import java.util.concurrent.TimeUnit;

/**
 * Created by tway on 4/4/16.
 */
public class ShelfUtils {

    public static boolean hasRxOnClasspath() {
        try {
            Class.forName("io.reactivex.Observable");
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
            throw new NoClassDefFoundError("RxJava2 is not on classpath, add it to your dependencies");
        }
    }

    public static boolean hasGsonOnClasspath() {
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

    public static Serializer defaultSerializer() {
        return hasGsonOnClasspath() ? new GsonSerializer() : new JavaSerializer();
    }

    public static long defaultLifetime() {
        return TimeUnit.MINUTES.toMillis(1);
    }
}
