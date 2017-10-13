package com.toddway.shelf.rx

import com.toddway.shelf.ShelfItem
import rx.Observable
import java.util.*

object ShelfSubjectFactory {

    fun <T> create(shelfItem: ShelfItem, type: Class<T>): CacheSubject<T> {
        return CacheSubject.create(object : CacheSubject.Source<T> {
            override fun read(): Observable<T> {
                return Observable.fromCallable { shelfItem.get(type) }
            }

            override fun write(t: T): Observable<T> {
                return observeWrite(shelfItem, t)
            }
        })
    }

    fun <T> createForList(shelfItem: ShelfItem, type: Class<Array<T>>): CacheSubject<List<T>> {
        return CacheSubject.create(object : CacheSubject.Source<List<T>> {
            override fun read(): Observable<List<T>> {
                return Observable.fromCallable {
                    val array = shelfItem.get(type)
                    if (array == null) ArrayList() else Arrays.asList(*array)
                }
            }

            override fun write(t: List<T>): Observable<List<T>> {
                return observeWrite(shelfItem, t)
            }
        })
    }

    private fun <T> observeWrite(shelfItem: ShelfItem, t: T): Observable<T> {
        return Observable.fromCallable {
            shelfItem.put(t)
            t
        }
    }
}
