package com.toddway.shelf

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.io.File

/**
 * Created by tway on 3/12/16.
 */
class CacheOrNewTests {

    internal lateinit var item: ShelfItem
    internal lateinit var subscriber: TestSubscriber<String>
    internal lateinit var shelf: Shelf
    internal var newValue: String? = null
    internal lateinit var cacheValue: String

    @Before
    fun beforeEach() {
        shelf = Shelf(File("/tmp"))
        item = shelf.item("string")
        subscriber = TestSubscriber()
        newValue = "new value"
        cacheValue = "cache value"
    }

    private fun givenNoCache() {
        item.clear()
    }

    private fun givenValidCache() {
        item.maxAge(5000).put(cacheValue)
    }

    private fun givenInvalidCache() {
        item.maxAge(0).put(cacheValue)
    }

    private fun givenNoNew() {
        newValue = null
    }

    private fun printValues(values: List<String>) {
        for (value in values) println("value: " + value)
    }

    private fun whenCacheOrNewSubscription() {
        Observable.just<String>(newValue)
                .compose(item.cacheOrNew(String::class.java))
                .subscribe(subscriber)
    }

    @Test
    fun testNoCache() {
        givenNoCache()
        whenCacheOrNewSubscription()

        subscriber.assertValues(newValue!!)
        assertEquals(item[String::class.java], newValue)
        printValues(subscriber.onNextEvents)
    }


    @Test
    fun testValidCache() {
        givenValidCache()
        whenCacheOrNewSubscription()

        subscriber.assertValues(cacheValue)
        subscriber.assertNoErrors()
        assertEquals(item[String::class.java], cacheValue)
        printValues(subscriber.onNextEvents)
    }

    @Test
    fun testInvalidCache() {
        givenInvalidCache()
        whenCacheOrNewSubscription()

        subscriber.assertValues(newValue!!)
        assertEquals(item[String::class.java], newValue)
        printValues(subscriber.onNextEvents)
    }

    @Test
    fun testNoCacheAndNoNew() {
        givenNoCache()
        givenNoNew()
        whenCacheOrNewSubscription()

        subscriber.assertNoValues()
        assertEquals(item[String::class.java], null)
        printValues(subscriber.onNextEvents)
    }

    @Test
    fun testNoNewAndInvalidCache() {
        givenNoNew()
        givenInvalidCache()
        whenCacheOrNewSubscription()

        subscriber.assertNoValues()
        assertEquals(item[String::class.java], cacheValue)
        printValues(subscriber.onNextEvents)
    }
}
