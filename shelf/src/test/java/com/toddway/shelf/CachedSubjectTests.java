package com.toddway.shelf;

import com.toddway.shelf.rx.CacheSubject;

import org.junit.Test;

import java.util.Arrays;

import rx.observers.TestSubscriber;

/**
 * Created by tway on 1/11/17.
 */

public class CachedSubjectTests extends BaseTest {

    CacheSubject<String> itemCache;
    CacheSubject<String[]> listCache;
    TestSubscriber<String[]> listSubscriber;
    ShelfItem listItem;

    @Override
    public void beforeEach() {
        super.beforeEach();
        listItem = shelf.item("list");
        itemCache = item.subject(String.class);
        listCache = listItem.subject(String[].class);
        listSubscriber = new TestSubscriber<>();
    }


    @Test public void testNoCache() {
        givenNoCache();
        itemCache.subscribe(subscriber);

        subscriber.assertValue(null);

        itemCache.onNext(newValue);

        subscriber.assertValues(null, newValue);

        itemCache.onNext(newValue);

        subscriber.assertValues(null, newValue, newValue);

        //printValues(subscriber.getOnNextEvents());
    }


    @Test public void testWithCache() {
        givenInvalidCache();
        itemCache.subscribe(subscriber);

        subscriber.assertValue(cacheValue);

        itemCache.onNext(newValue);

        subscriber.assertValues(cacheValue, newValue);

        itemCache.onNext(newValue);

        subscriber.assertValues(cacheValue, newValue, newValue);

        //printValues(subscriber.getOnNextEvents());
    }

    @Test public void testList() {
        listItem.clear();
        listSubscriber.assertNoValues();

        listCache.subscribe(listSubscriber);
        listSubscriber.assertValue(null);

        //itemCache.onError(new RuntimeException("test"));

        String[] array = {"asfdf", "Asdfasdf", "adsfsadf"};
        listCache.onNext(array);
        listSubscriber.assertValues(null, array);

        beforeEach(); //reinit

        listCache.subscribe(listSubscriber);
        listSubscriber.assertValueCount(1);

        System.out.println("\nlist values...");
        for (String[] a : listSubscriber.getOnNextEvents()) {
            if (a == null)System.out.println("null");
            else printValues(Arrays.asList(a));
        }


    }
}
