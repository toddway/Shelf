package com.toddway.shelf

import com.toddway.shelf.rx.getObservable
import org.junit.Test

/**
 *
 * Created by nschwermann on 12/21/17.
 */
class MaybeTests : BaseTest() {

    private fun whenSubscribe(){
        item.getObservable(String::class.java)
                .subscribe(subscriber)
    }

    @Test
    fun testNoCache(){
        givenNoCache()
        whenSubscribe()
        subscriber.assertNoErrors()
        subscriber.assertNoValues()
        subscriber.assertComplete()
    }

    @Test
    fun testInvalidCache(){
        givenInvalidCache()
        whenSubscribe()
        subscriber.assertNoErrors()
        subscriber.assertNoValues()
        subscriber.assertComplete()
    }

    @Test
    fun testValidCache(){
        givenValidCache()
        whenSubscribe()
        subscriber.assertNoErrors()
        subscriber.assertValue(cacheValue)
        //https://github.com/ReactiveX/RxJava/issues/5774
//        subscriber.assertNotComplete()
    }

}