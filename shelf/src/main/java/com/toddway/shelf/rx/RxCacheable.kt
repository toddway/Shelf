package com.toddway.shelf.rx

import rx.Observable
import rx.functions.Func1
import java.util.concurrent.TimeUnit

/**
 * Created by tway on 3/10/16.
 */
abstract class RxCacheable<T>(private val observeNew: Observable<T>) {

    abstract var cache: T?
    abstract val isCacheValid: Boolean

    val isNotNull: Func1<T, Boolean>
        get() = Func1 { data -> data != null }

    val isNull: Func1<T, Boolean>
        get() = Func1 { data -> data == null }

    fun observeCache(): Observable<T> {
        return Observable.fromCallable { cache }
    }

    fun observeCacheIfValid(): Observable<T> {
        return Observable.fromCallable { if (isCacheValid) cache else null }
    }

    fun observeNewOnly(): Observable<T> {
        return observeNew.doOnNext { t -> if (t != null) cache = t }
    }

    fun observeNewIfCacheNotValid(): Observable<T> {
        return Observable.fromCallable { if (isCacheValid) null else observeNewOnly().toBlocking().first() }
    }

    fun observeCacheThenNew(): Observable<T> {
        //return observeCache().concatWith(observeCacheOrNew());
        return Observable.concat(observeCache(), observeNewIfCacheNotValid().skipWhile(isNull))
    }

    fun observeCacheOrNew(): Observable<T> {
        return Observable.concat(observeCacheIfValid(), observeNewOnly()).first(isNotNull)
    }

    fun pollNew(value: Long, unit: TimeUnit): Observable<T> {
        return observeNewOnly().repeatWhen(interval(value, unit))
    }

    fun interval(value: Long, unit: TimeUnit): Func1<Observable<out Void>, Observable<*>> {
        return Func1 { observable -> observable.delay(value, unit) }
    }

    fun observeCacheThenPollNew(value: Long, unit: TimeUnit): Observable<T> {
        return Observable.concat(observeCache(), pollNew(value, unit))
    }

    fun observeFirst(): Observable<T> {
        return Observable.concat(observeCache(), observeNewOnly()).first(isNotNull)
    }
}
