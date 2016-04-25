package com.toddway.shelf;

import com.toddway.shelf.storage.FileStorage;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

/**
 * Created by tway on 3/12/16.
 */
public class RxShelfTests {


    ShelfItem shelfItem;
    TestSubscriber<String> testSubscriber;
    Shelf shelf;
    String string = "new value";

    @Before
    public void beforeEach() {
        shelf = new Shelf(new FileStorage(new File("/tmp"), ShelfUtils.defaultSerializer(), 5000));
        shelfItem = shelf.item("string");
    }

    public void givenNoCache() {
        shelfItem.clear();
        testSubscriber = new TestSubscriber<>();
    }

    public void givenValidCache() {
        givenNoCache();
        shelfItem.put("initial cache");
        shelfItem.lifetime(5000);
    }

    public void givenInvalidCache() {
        givenValidCache();
        shelfItem.lifetime(0);
    }

    public void givenNoNew() {
        string = null;
        testSubscriber = new TestSubscriber<>();
    }

    @Test
    public void testObserveCacheThenFirstValid() {
        givenNoCache();
        Observable.just(string)
                .compose(shelfItem.cacheThenNew(String.class))
                .subscribe(testSubscriber);
        testSubscriber.assertValues(null, "new value");
        assertEquals(shelfItem.get(String.class), "new value");

        givenValidCache();
        Observable.just(string)
                .compose(shelfItem.cacheThenNew(String.class))
                .subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache");
        assertEquals(shelfItem.get(String.class), "initial cache");

        givenInvalidCache();
        Observable.just(string)
                .compose(shelfItem.cacheThenNew(String.class))
                .subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache", "new value");
        assertEquals(shelfItem.get(String.class), "new value");

        givenNoCache();
        givenNoNew();
        Observable.just(string)
                .compose(shelfItem.cacheThenNew(String.class))
                .subscribe(testSubscriber);
        testSubscriber.assertValue(null);
        assertEquals(shelfItem.get(String.class), null);

        givenNoNew();
        givenInvalidCache();
        Observable.just(string)
                .compose(shelfItem.cacheThenNew(String.class))
                .subscribe(testSubscriber);
        testSubscriber.assertValues("initial cache");
    }


    @Ignore
    @Test
    public void testObserveCacheThenPollNew() {
        givenInvalidCache();
        Observable.just(string)
                .compose(shelf.item("string").cacheThenPollNew(String.class, 1, TimeUnit.NANOSECONDS))
                .doOnNext(printString())
                .subscribe(testSubscriber);

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
