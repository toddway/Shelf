package com.toddway.shelf


import com.toddway.shelf.rx.RxCacheable
import rx.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by tway on 4/24/16.
 */
class Shelfable<T>(observeNew: Observable<T>, internal var item: ShelfItem, internal var type: Class<T>) : RxCacheable<T>(observeNew) {

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

        fun <T> cacheThenNew(item: ShelfItem, type: Class<T>): Observable.Transformer<T, T> {
            return Observable.Transformer { observable -> Shelfable(observable, item, type).observeCacheThenNew() }
        }

        fun <T> cacheOrNew(item: ShelfItem, type: Class<T>): Observable.Transformer<T, T> {
            return Observable.Transformer { observable -> Shelfable(observable, item, type).observeCacheOrNew() }
        }

        fun <T> newOnly(item: ShelfItem, type: Class<T>): Observable.Transformer<T, T> {
            return Observable.Transformer { observable -> Shelfable(observable, item, type).observeNewOnly() }
        }

        fun <T> pollNew(item: ShelfItem, type: Class<T>, value: Long, unit: TimeUnit): Observable.Transformer<T, T> {
            return Observable.Transformer { observable -> Shelfable(observable, item, type).pollNew(value, unit) }
        }

        fun <T> cacheThenPollNew(item: ShelfItem, type: Class<T>, value: Long, unit: TimeUnit): Observable.Transformer<T, T> {
            return Observable.Transformer { observable -> Shelfable(observable, item, type).observeCacheThenPollNew(value, unit) }
        }
    }
}
