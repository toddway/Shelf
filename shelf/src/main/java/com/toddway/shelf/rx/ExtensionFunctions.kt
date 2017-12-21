package com.toddway.shelf.rx

import com.toddway.shelf.ShelfItem
import io.reactivex.Maybe
import io.reactivex.Single

/**
 *
 * Created by nschwermann on 12/20/17.
 */

fun <T> Single<T>.cacheOrNew(item: ShelfItem, type: Class<T>): Single<T> {
    return compose(Shelfable.cacheOrNew(item, type))
}

fun <T> ShelfItem.getObservable(type: Class<T>): Maybe<T> {
    return Shelfable.observeCache(this, type)
}