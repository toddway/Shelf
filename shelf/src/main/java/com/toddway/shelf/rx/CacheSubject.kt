package com.toddway.shelf.rx

import rx.Observable
import rx.functions.Action1
import rx.subjects.BehaviorSubject
import rx.subjects.Subject

class CacheSubject<T> protected constructor(onSubscribe: Observable.OnSubscribe<T>, private val cacheSource: Source<T>, private val behaviorSubject: BehaviorSubject<T>) : Subject<T, T>(onSubscribe) {

    val value: T
        get() = behaviorSubject.value

    interface Source<T> {
        fun read(): Observable<T>
        fun write(t: T): Observable<T>
    }

    override fun hasObservers(): Boolean {
        return behaviorSubject.hasObservers()
    }

    override fun onCompleted() {
        behaviorSubject.onCompleted()
    }

    override fun onError(e: Throwable) {
        behaviorSubject.onError(e)
    }

    override fun onNext(t: T) {
        updateCache(t).subscribe()
    }

    fun updateCache(t: T): Observable<T> {
        return cacheSource.write(t).doOnNext(emitOnNext(behaviorSubject))
    }

    companion object {

        fun <T> create(source: Source<T>): CacheSubject<T> {
            val subject = BehaviorSubject.create<T>()

            val onSubscribe = Observable.OnSubscribe<T> { subscriber ->
                subject.subscribe(subscriber)
                if (!subject.hasValue()) {
                    source.read().subscribe(emitOnNext(subject))
                }
            }

            return CacheSubject(onSubscribe, source, subject)
        }

        private fun <T> emitOnNext(behaviorSubject: BehaviorSubject<T>): Action1<T> {
            return Action1 { t -> behaviorSubject.onNext(t) }
        }
    }
}
