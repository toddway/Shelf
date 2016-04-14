package com.toddway.shelf;

import com.toddway.shelf.storage.FileStorage;

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

    ShelfItem.Policies<String> itemPolicies;
    ShelfItem<String> item;
    TestSubscriber<String> testSubscriber;
    Shelf shelf;
    String string = "new value";

    @Before
    public void beforeEach() {
        shelf = new Shelf(new FileStorage(new File("/tmp"), ShelfUtils.defaultSerializer(), 5000));
        item = shelf.item("my string");
        itemPolicies = item.policies(String.class, observable());
    }

    public void givenNoCache() {
        item.clear();
        testSubscriber = new TestSubscriber<>();
    }

    public void givenValidCache() {
        givenNoCache();
        item.put("initial cache");
        item.setLifetime(5000);
    }

    public void givenInvalidCache() {
        givenValidCache();
        item.setLifetime(0);
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
        itemPolicies.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues(null, "new value");
        assertEquals(itemPolicies.getCache(), "new value");

        givenValidCache();
        itemPolicies.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache");
        assertEquals(itemPolicies.getCache(), "initial cache");

        givenInvalidCache();
        itemPolicies.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache", "new value");
        assertEquals(itemPolicies.getCache(), "new value");

        givenNoCache();
        givenNoNew();
        itemPolicies.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValue(null);
        assertEquals(itemPolicies.getCache(), null);

        givenNoNew();
        givenInvalidCache();
        itemPolicies.observeCacheThenNew().subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache");
    }


    @Ignore
    @Test
    public void testObserveCacheThenPollNew() {
        givenInvalidCache();
        itemPolicies.observeCacheThenPollNew(1, TimeUnit.NANOSECONDS).doOnNext(printString()).subscribe(testSubscriber);
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
