package com.toddway.shelf

import com.toddway.shelf.rx.cacheOrNew
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * Created by tway on 3/12/16.
 * updated nschwermann 12/21/17
 */
class CacheOrNewTests : BaseTest(){

    private val noElementError : IOException = IOException()

    private fun whenCacheOrNewSubscription() {

        Single.defer { newValue?.let { Single.just(it) } ?: Single.error(noElementError) }
                .cacheOrNew(item, String::class.java)
                .subscribe(subscriber)
    }

    @Test
    fun testNoCache() {
        givenNoCache()
        whenCacheOrNewSubscription()

        subscriber.assertValues(newValue!!)
        assertEquals(item[String::class.java], newValue)
        printValues(subscriber.values())
    }


    @Test
    fun testValidCache() {
        givenValidCache()
        whenCacheOrNewSubscription()

        subscriber.assertValues(cacheValue)
        subscriber.assertNoErrors()
        assertEquals(item[String::class.java], cacheValue)
        printValues(subscriber.values())
    }

    @Test
    fun testInvalidCache() {
        givenInvalidCache()
        whenCacheOrNewSubscription()

        subscriber.assertValues(newValue!!)
        assertEquals(item[String::class.java], newValue)
        printValues(subscriber.values())
    }

    @Test
    fun testNoCacheAndNewError() {
        givenNoCache()
        givenNoNew()
        whenCacheOrNewSubscription()

        subscriber.assertNoValues()
        subscriber.assertError(noElementError)
    }

    @Test
    fun testNewErrorAndInvalidCache() {
        givenNoNew()
        givenInvalidCache()
        whenCacheOrNewSubscription()

        subscriber.assertNoValues()
        subscriber.assertError(noElementError)
        assertEquals(item[String::class.java], cacheValue)
        printValues(subscriber.values())
    }
}
