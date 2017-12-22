package com.toddway.shelf

import io.reactivex.observers.TestObserver
import org.junit.Before
import java.io.File

/**
 * Created by tway on 1/11/17.
 * updated nschwermann 12/21/17
 */

abstract class BaseTest {

    lateinit var item: ShelfItem
    lateinit var subscriber: TestObserver<String>
    lateinit var shelf: Shelf
    var newValue: String? = null
    lateinit var cacheValue: String

    @Before
    open fun beforeEach() {
        shelf = Shelf(File("/tmp"))
        item = shelf.item("string")
        subscriber = TestObserver()
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
        for (value in values) println("value: " + value)
    }

}
