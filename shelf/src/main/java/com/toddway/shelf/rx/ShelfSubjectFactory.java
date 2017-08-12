package com.toddway.shelf.rx;

import com.toddway.shelf.ShelfItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

public class ShelfSubjectFactory {

    public static <T> CacheSubject<T> create(final ShelfItem shelfItem, final Class<T> type) {
        return CacheSubject.create(new CacheSubject.Source<T>() {
            @Override
            public Observable<T> read() {
                return Observable.fromCallable(new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        return shelfItem.get(type);
                    }
                });
            }

            @Override
            public Observable<T> write(final T t) {
                return observeWrite(shelfItem, t);
            }
        });
    }

    public static <T> CacheSubject<List<T>> createForList(final ShelfItem shelfItem, final Class<T[]> type) {
        return CacheSubject.create(new CacheSubject.Source<List<T>>() {
            @Override
            public Observable<List<T>> read() {
                return Observable.fromCallable(new Callable<List<T>>() {
                    @Override
                    public List<T> call() throws Exception {
                        T[] array = shelfItem.get(type);
                        return array == null ? new ArrayList<T>() : Arrays.asList(array);
                    }
                });
            }

            @Override
            public Observable<List<T>> write(final List<T> t) {
                return observeWrite(shelfItem, t);
            }
        });
    }

    private static <T> Observable<T> observeWrite(final ShelfItem shelfItem, final T t) {
        return Observable.fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                shelfItem.put(t);
                return t;
            }
        });
    }
}
