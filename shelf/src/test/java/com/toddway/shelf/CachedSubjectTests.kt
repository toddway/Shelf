package com.toddway.shelf

import com.toddway.shelf.rx.CacheSubject
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

/**
 * Created by tway on 1/11/17.
 */

class CachedSubjectTests : BaseTest() {

    internal lateinit var itemCache: CacheSubject<String>
    internal lateinit var listCache: CacheSubject<Array<String>>
    internal lateinit var listSubscriber: TestSubscriber<Array<String>>
    internal lateinit var listItem: ShelfItem

    override fun beforeEach() {
        super.beforeEach()
        listItem = shelf.item("list")
        itemCache = item.subject(String::class.java)
        listCache = listItem.subject(Array<String>::class.java)
        listSubscriber = TestSubscriber()
    }


    @Test
    fun testNoCache() {
        givenNoCache()
        itemCache.subscribe(subscriber)

        subscriber.assertValue(null)

        newValue?.let { itemCache.onNext(it) }

        subscriber.assertValues(null, newValue)

        newValue?.let { itemCache.onNext(it) }

        subscriber.assertValues(null, newValue, newValue)

        //printValues(subscriber.getOnNextEvents());
    }


    @Test
    fun testWithCache() {
        givenInvalidCache()
        itemCache.subscribe(subscriber)

        subscriber.assertValue(cacheValue)

        newValue?.let { itemCache.onNext(it) }

        subscriber.assertValues(cacheValue, newValue)

        newValue?.let { itemCache.onNext(it) }

        subscriber.assertValues(cacheValue, newValue, newValue)

        //printValues(subscriber.getOnNextEvents());
    }

    @Test
    fun testList() {
        listItem.clear()
        listSubscriber.assertNoValues()

        listCache.subscribe(listSubscriber)
        listSubscriber.assertValue(null)

        //itemCache.onError(new RuntimeException("test"));

        val array = arrayOf("asfdf", "Asdfasdf", "adsfsadf")
        listCache.onNext(array)
        listSubscriber.assertValues(null, array)

        beforeEach() //reinit

        listCache.subscribe(listSubscriber)
        listSubscriber.assertValueCount(1)

        println("\nlist values...")
        for (a in listSubscriber.onNextEvents) {
            if (a == null)
                println("null")
            else
                printValues(Arrays.asList(*a))
        }


    }
}
