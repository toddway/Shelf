package com.toddway.shelf.rx

import io.reactivex.Maybe

/**
 * Created by tway on 3/10/16.
 * updated nschwermann 12/20/17
 */
abstract class RxCacheable<T>(private val observeNew: Maybe<T>) {

    abstract var cache: T?
    abstract val isCacheValid: Boolean

    private fun observeCache(): Maybe<T> {
        return Maybe.defer{ cache?.let { Maybe.just(it) } ?: Maybe.empty() }
    }

    fun observeCacheIfValid(): Maybe<T> {
        return Maybe.defer { if (isCacheValid) observeCache() else Maybe.empty() }
    }

    fun observeNewOnly(): Maybe<T> {
        return observeNew.doOnSuccess { cache = it }
    }

    fun observeCacheOrNew(): Maybe<T> {
        return observeCacheIfValid().switchIfEmpty(observeNewOnly())
    }
}
