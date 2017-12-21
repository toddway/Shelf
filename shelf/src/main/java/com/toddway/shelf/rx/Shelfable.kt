package com.toddway.shelf.rx


import com.toddway.shelf.ShelfItem
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.SingleTransformer

/**
 * Created by tway on 4/24/16.
 * updated nschwermann 12/21/17
 */
class Shelfable<T>(observeNew: Single<T>, internal var item: ShelfItem, internal var type: Class<T>) : RxCacheable<T>(observeNew.toMaybe()) {

    override var cache: T?
        get() = item.get(type)
        set(t) {
            item.put(t)
        }

    override val isCacheValid: Boolean
        get() = !item.isOlderThanLifetime

    fun item(): ShelfItem {
        return item
    }

    companion object {

        internal fun<T> observeCache(item: ShelfItem, type: Class<T>) : Maybe<T> {
            return Shelfable(Maybe.empty<T>().toSingle(), item, type).observeCacheIfValid()
        }

        internal fun<T> cacheOrNew(item: ShelfItem, type: Class<T>): SingleTransformer<T, T> {
            return SingleTransformer { observable -> Shelfable(observable, item, type).observeCacheOrNew().toSingle() }
        }
    }
}
