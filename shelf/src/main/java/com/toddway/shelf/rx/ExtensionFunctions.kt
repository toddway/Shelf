package com.toddway.shelf.rx

import com.toddway.shelf.ShelfItem
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Adds RX functionality to Shelf and Shelf functionality to RX
 */

fun <T> Single<T>.cacheOrNew(item: ShelfItem, type: Class<T>): Single<T> {
    return compose(SuccessCacheable.cacheOrNew(item, type))
}

fun <T> ShelfItem.maybe(type: Class<T>): Maybe<T> {
    return SuccessCacheable.observeCache(this, type)
}