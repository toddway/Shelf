package com.toddway.shelf;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertEquals;

/**
 * Created by tway on 1/9/17.
 */

public class CacheThenNewTests {

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

    private void givenNoCache() {
        item.clear();
    }

    private void givenValidCache() {
        item.maxAge(5000).put(cacheValue);
    }

    private void givenInvalidCache() {
        item.maxAge(0, TimeUnit.MINUTES).put(cacheValue);
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

        // TODO New TestObserver does not contain the getOnNextEvents method, nor a clear similar
        // method.  Call has been commented out to allow compiling, until such time as a replacement
        // is built into RxJava2.
        // printValues(subscriber.getOnNextEvents());
    }

    @Test
    public void testValidCache() {
        givenValidCache();
        whenCacheThenNewSubscription();

        subscriber.assertValues(cacheValue);
        assertEquals(item.get(String.class), cacheValue);
        // TODO New TestObserver does not contain the getOnNextEvents method, nor a clear similar
        // method.  Call has been commented out to allow compiling, until such time as a replacement
        // is built into RxJava2.
        // printValues(subscriber.getOnNextEvents());
    }

    @Test
    public void testInvalidCache() {
        givenInvalidCache();
        whenCacheThenNewSubscription();

        subscriber.assertValues(cacheValue, newValue);
        assertEquals(item.get(String.class), newValue);
        // TODO New TestObserver does not contain the getOnNextEvents method, nor a clear similar
        // method.  Call has been commented out to allow compiling, until such time as a replacement
        // is built into RxJava2.
        // printValues(subscriber.getOnNextEvents());
    }

    @Test public void testNoCacheAndNoNew() {
        givenNoCache();
        givenNoNew();
        whenCacheThenNewSubscription();

        subscriber.assertValue((String)null);
        assertEquals(item.get(String.class), null);
        // TODO New TestObserver does not contain the getOnNextEvents method, nor a clear similar
        // method.  Call has been commented out to allow compiling, until such time as a replacement
        // is built into RxJava2.
        // printValues(subscriber.getOnNextEvents());
    }

    @Test public void  testNoNewAndInvalidCache() {
        givenNoNew();
        givenInvalidCache();
        whenCacheThenNewSubscription();

        subscriber.assertValues(cacheValue);
        assertEquals(item.get(String.class), cacheValue);
        // TODO New TestObserver does not contain the getOnNextEvents method, nor a clear similar
        // method.  Call has been commented out to allow compiling, until such time as a replacement
        // is built into RxJava2.
        // printValues(subscriber.getOnNextEvents());
    }
}
