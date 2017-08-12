package com.toddway.shelf;

import com.toddway.shelf.rx.RxCacheable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

/**
 * Created by tway on 4/24/16.
 */
public class Shelfable<T> extends RxCacheable<T> {
    ShelfItem item;
    Class<T> type;

    protected Shelfable(Observable<T> observeNew, ShelfItem item, Class<T> type) {
        super(observeNew);
        this.item = item;
        this.type = type;
    }

    public ShelfItem item() {
        return item;
    }

    @Override
    public T getCache() {
        return item.get(type);
    }

    @Override
    public boolean isCacheValid() {
        return !item.isOlderThanLifetime();
    }

    @Override
    public void setCache(T t) {
        item.put(t);
    }

    public static <T> ObservableTransformer<T, T> cacheThenNew(final ShelfItem item, final Class<T> type) {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).observeCacheThenNew();
            }
        };
    }

    public static <T> ObservableTransformer<T, T> cacheOrNew(final ShelfItem item, final Class<T> type) {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return (new Shelfable<>(observable, item, type).observeCacheOrNew()).toObservable();
            }
        };
    }

    public static <T> ObservableTransformer<T, T> newOnly(final ShelfItem item, final Class<T> type) {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).observeNewOnly();
            }
        };
    }

    public static <T> ObservableTransformer<T, T> pollNew(final ShelfItem item, final Class<T> type, final long value, final TimeUnit unit) {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).pollNew(value, unit);
            }
        };
    }

    public static <T> ObservableTransformer<T, T> cacheThenPollNew(final ShelfItem item, final Class<T> type, final long value, final TimeUnit unit) {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).observeCacheThenPollNew(value, unit);
            }
        };
    }
}
