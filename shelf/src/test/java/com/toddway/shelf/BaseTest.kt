package com.toddway.shelf

import org.junit.Before
import rx.observers.TestSubscriber
import java.io.File

/**
 * Created by tway on 1/11/17.
 */

open class BaseTest {

    lateinit var item: ShelfItem
    lateinit var subscriber: TestSubscriber<String>
    lateinit var shelf: Shelf
    var newValue: String? = null
    lateinit var cacheValue: String

    @Before
    open fun beforeEach() {
        shelf = Shelf(File("/tmp"))
        item = shelf.item("string")
        subscriber = TestSubscriber()
        newValue = "new value"
        cacheValue = "cache value"
    }

    fun givenNoCache() {
        item.clear()
    }

    fun givenValidCache() {
        item.maxAge(5000).put(cacheValue)
    }

    fun givenInvalidCache() {
        item.maxAge(0).put(cacheValue)
    }

    fun givenNoNew() {
        newValue = null
    }

    fun <T> printValues(values: List<T>) {
        println("values...")
        for (value in values) println("value: " + value)
    }

}
