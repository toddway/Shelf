package com.toddway.shelf;

import org.junit.Before;

import java.io.File;
import java.util.List;

import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;

/**
 * Created by tway on 1/11/17.
 */

public class BaseTest {

    ShelfItem item;
    TestObserver<String> subscriber;
    Shelf shelf;
    String newValue;
    String cacheValue;

    @Before
    public void beforeEach() {
        shelf = new Shelf(new File("/tmp"));
        item = shelf.item("string");
        subscriber = new TestObserver<>();
        newValue = "new value";
        cacheValue = "cache value";
    }

    public void givenNoCache() {
        item.clear();
    }

    public void givenValidCache() {
        item.maxAge(5000).put(cacheValue);
    }

    public void givenInvalidCache() {
        item.maxAge(0).put(cacheValue);
    }

    public void givenNoNew() {
        newValue = null;
    }

    public <T> void printValues(List<T> values) {
        System.out.println("values...");
        for (T value : values) System.out.println("value: " + value);
    }

}
