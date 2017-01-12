package com.toddway.shelf;

import com.toddway.shelf.rx.CacheSubject;

import org.junit.Test;

import java.util.Arrays;

import rx.observers.TestSubscriber;

/**
 * Created by tway on 1/11/17.
 */

public class CachedSubjectTests extends BaseTest {

    CacheSubject<String> subject;
    CacheSubject<String[]> listSubject;
    TestSubscriber<String[]> listSubscriber;
    ShelfItem listItem;

    @Override
    public void beforeEach() {
        super.beforeEach();
        listItem = shelf.item("list");
        subject = item.subject(String.class);
        listSubject = listItem.subject(String[].class);
        listSubscriber = new TestSubscriber<>();
    }


    @Test public void testNoCache() {
        givenNoCache();
        subject.subscribe(subscriber);

        subscriber.assertValue(null);

        subject.onNext(newValue);

        subscriber.assertValues(null, newValue);

        subject.onNext(newValue);

        subscriber.assertValues(null, newValue, newValue);

        //printValues(subscriber.getOnNextEvents());
    }


    @Test public void testWithCache() {
        givenInvalidCache();
        subject.subscribe(subscriber);

        subscriber.assertValue(cacheValue);

        subject.onNext(newValue);

        subscriber.assertValues(cacheValue, newValue);

        subject.onNext(newValue);

        subscriber.assertValues(cacheValue, newValue, newValue);

        //printValues(subscriber.getOnNextEvents());
    }

    @Test public void testList() {
        listItem.clear();
        listSubscriber.assertNoValues();

        listSubject.subscribe(listSubscriber);
        listSubscriber.assertValue(null);

        //subject.onError(new RuntimeException("test"));

        String[] array = {"asfdf", "Asdfasdf", "adsfsadf"};
        listSubject.onNext(array);
        listSubscriber.assertValues(null, array);

        beforeEach(); //reinit

        listSubject.subscribe(listSubscriber);
        listSubscriber.assertValueCount(1);

        System.out.println("\nlist values...");
        for (String[] a : listSubscriber.getOnNextEvents()) {
            if (a == null)System.out.println("null");
            else printValues(Arrays.asList(a));
        }


    }
}
