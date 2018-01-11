package com.toddway.shelf.rx

import com.toddway.shelf.ShelfItem
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.SingleTransformer

/**
 * Adds RX functionality to Shelf and Shelf functionality to RX
 */

/**
 * Causes a single source to use shelf cache if it is valid and then falls back to source.
 * @param item - shelf item containing the cache
 * @param type - type that shelf should deserialize to
 * @return the original [Single] composed with cacheOrNew
 */
fun <T> Single<T>.cacheOrNew(item: ShelfItem, type: Class<T>): Single<T> {
    return compose(com.toddway.shelf.rx.cacheOrNew(item, type))
}

/**
 * Creates a [Maybe] that will return the value of the [ShelfItem] or empty
 * @param type type that shelf should deserialize to
 */
fun <T> ShelfItem.maybe(type: Class<T>): Maybe<T> {
    return SuccessCacheable.observeCache(this, type)
}

//Java Interface

/**
 * Java interface for [cacheOrNew]
 */
fun <T> cacheOrNew(item: ShelfItem, type: Class<T>): SingleTransformer<T, T> {
    return SuccessCacheable.cacheOrNew(item, type)
}