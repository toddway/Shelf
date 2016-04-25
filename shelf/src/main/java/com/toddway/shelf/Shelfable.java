package com.toddway.shelf;

import java.util.concurrent.TimeUnit;

import rx.Observable;

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

    public static <T> Observable.Transformer<T, T> cacheThenNew(final ShelfItem item, final Class<T> type) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).observeCacheThenNew();
            }
        };
    }

    public static <T> Observable.Transformer<T, T> cacheOrNew(final ShelfItem item, final Class<T> type) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).observeCacheOrNew();
            }
        };
    }

    public static <T> Observable.Transformer<T, T> newOnly(final ShelfItem item, final Class<T> type) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).observeNewOnly();
            }
        };
    }

    public static <T> Observable.Transformer<T, T> pollNew(final ShelfItem item, final Class<T> type, final long value, final TimeUnit unit) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).pollNew(value, unit);
            }
        };
    }

    public static <T> Observable.Transformer<T, T> cacheThenPollNew(final ShelfItem item, final Class<T> type, final long value, final TimeUnit unit) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return new Shelfable<>(observable, item, type).observeCacheThenPollNew(value, unit);
            }
        };
    }
}
