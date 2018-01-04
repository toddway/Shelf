package com.toddway.shelf.rx

import com.toddway.shelf.ShelfItem
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.SingleTransformer

/**
 * Adds RX functionality to Shelf and Shelf functionality to RX
 */

fun <T> Single<T>.cacheOrNew(item: ShelfItem, type: Class<T>): Single<T> {
    return compose(SuccessCacheable.cacheOrNew(item, type))
}

fun <T> ShelfItem.maybe(type: Class<T>): Maybe<T> {
    return SuccessCacheable.observeCache(this, type)
}

//Java Interface

fun<T> cacheOrNew(item: ShelfItem, type: Class<T>): SingleTransformer<T, T> {
    return SuccessCacheable.cacheOrNew(item, type)
}

fun<T> maybeShelf(item: ShelfItem, type: Class<T>): Maybe<T> {
    return SuccessCacheable.observeCache(item, type)
}