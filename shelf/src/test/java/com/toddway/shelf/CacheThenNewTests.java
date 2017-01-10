package com.toddway.shelf;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

/**
 * Created by tway on 1/9/17.
 */

public class CacheThenNewTests {

    ShelfItem item;
    TestSubscriber<String> subscriber;
    Shelf shelf;
    String newValue;
    String cacheValue;

    @Before
    public void beforeEach() {
        shelf = new Shelf(new File("/tmp"));
        item = shelf.item("string");
        subscriber = new TestSubscriber<>();
        newValue = "new value";
        cacheValue = "cache value";
    }

    private void givenNoCache() {
        item.clear();
    }

    private void givenValidCache() {
        item.lifetime(5000).put(cacheValue);
    }

    private void givenInvalidCache() {
        item.lifetime(0).put(cacheValue);
    }

    private void givenNoNew() {
        newValue = null;
    }

    private void printValues(List<String> values) {
        for (String value : values) System.out.println("value: " + value);
    }

    private void whenCacheThenNewSubscription() {
        Observable.just(newValue)
                .compose(item.cacheThenNew(String.class))
                .subscribe(subscriber);
    }

    @Test
    public void testNoCache() {
        givenNoCache();
        whenCacheThenNewSubscription();

        subscriber.assertValues(null, newValue);
        assertEquals(item.get(String.class), newValue);

        printValues(subscriber.getOnNextEvents());
    }

    @Test
    public void testValidCache() {
        givenValidCache();
        whenCacheThenNewSubscription();

        subscriber.assertValues(cacheValue);
        assertEquals(item.get(String.class), cacheValue);
        printValues(subscriber.getOnNextEvents());
    }

    @Test
    public void testInvalidCache() {
        givenInvalidCache();
        whenCacheThenNewSubscription();

        subscriber.assertValues(cacheValue, newValue);
        assertEquals(item.get(String.class), newValue);
        printValues(subscriber.getOnNextEvents());
    }

    @Test public void testNoCacheAndNoNew() {
        givenNoCache();
        givenNoNew();
        whenCacheThenNewSubscription();

        subscriber.assertValue(null);
        assertEquals(item.get(String.class), null);
        printValues(subscriber.getOnNextEvents());
    }

    @Test public void  testNoNewAndInvalidCache() {
        givenNoNew();
        givenInvalidCache();
        whenCacheThenNewSubscription();

        subscriber.assertValues(cacheValue);
        assertEquals(item.get(String.class), cacheValue);
        printValues(subscriber.getOnNextEvents());
    }
}
