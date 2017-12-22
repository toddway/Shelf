package com.toddway.shelf.rx

import com.toddway.shelf.ShelfItem
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.SingleTransformer

/**
 * Cacheable impl that returns Maybe or Single objects
 */
internal class SuccessCacheable<T>(private val observeNew: Single<T>, shelfItem: ShelfItem, type: Class<T>) :
        Cacheable<T> by Shelfable(shelfItem, type){

    fun observeCacheOrNew(): Single<T> {
        return observeCacheIfValid().switchIfEmpty(observeNewOnly())
    }

    fun observeCacheIfValid(): Maybe<T> {
        return Maybe.defer { if (isCacheValid) observeCache() else Maybe.empty() }
    }

    private fun observeNewOnly(): Single<T> {
        return observeNew.doOnSuccess { cache = it }
    }

    private fun observeCache(): Maybe<T> {
        return Maybe.defer{ cache?.let { Maybe.just(it) } ?: Maybe.empty() }
    }

    companion object {

        internal fun<T> observeCache(item: ShelfItem, type: Class<T>) : Maybe<T> {
            return SuccessCacheable(Maybe.empty<T>().toSingle(), item, type).observeCacheIfValid()
        }

        internal fun<T> cacheOrNew(item: ShelfItem, type: Class<T>): SingleTransformer<T, T> {
            return SingleTransformer { observable -> SuccessCacheable(observable, item, type).observeCacheOrNew() }
        }
    }
}