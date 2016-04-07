package com.toddway.shelf;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

/**
 * Created by tway on 3/12/16.
 */
public class RxShelfTests {

    Shelf.Cacheable<String> cacheable;
    TestSubscriber<String> testSubscriber;
    Shelf shelf;
    String string = "new value";

    @Before
    public void beforeEach() {
        shelf = new Shelf(new File("/tmp")).setMaxAge(5000);
        cacheable = shelf.cacheable("MyString", String.class, observable());
    }

    public void givenNoCache() {
        cacheable.item().clear();
        testSubscriber = new TestSubscriber<>();
    }

    public void givenValidCache() {
        givenNoCache();
        cacheable.setCache("initial cache");
        cacheable.setMaxAge(5000);
    }

    public void givenInvalidCache() {
        givenValidCache();
        cacheable.setMaxAge(0);
    }

    public void givenNoNew() {
        string = null;
        testSubscriber = new TestSubscriber<>();
    }

    public Observable<String> observable() {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return string;
            }
        });
    }

    @Test
    public void testObserveCacheThenFirstValid() {
        givenNoCache();
        cacheable.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues(null, "new value");
        assertEquals(cacheable.getCache(), "new value");

        givenValidCache();
        cacheable.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache");
        assertEquals(cacheable.getCache(), "initial cache");

        givenInvalidCache();
        cacheable.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache", "new value");
        assertEquals(cacheable.getCache(), "new value");

        givenNoCache();
        givenNoNew();
        cacheable.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValue(null);
        assertEquals(cacheable.getCache(), null);

        givenNoNew();
        givenInvalidCache();
        cacheable.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache");
    }


    @Ignore
    @Test
    public void testObserveCacheThenPollNew() {
        givenInvalidCache();
        cacheable.observeCacheThenPollNew(1, TimeUnit.NANOSECONDS).doOnNext(printString()).subscribe(testSubscriber);
        System.out.println(testSubscriber.getOnNextEvents().size() + " - emits");
        testSubscriber.assertValues("initial cache", "new value", "new value");
        //assertTrue(testSubscriber.getOnNextEvents().size() > 2);
        //testSubscriber.assertValues("1");
    }

    public static Action1<String> printString() {
        return new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s +  " - emitted");
            }
        };
    }



}
