package com.toddway.shelf.rx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by tway on 3/10/16.
 */
public abstract class RxCacheable<T> {

    private Observable<T> observeNew;

    public RxCacheable(Observable<T> observeNew) {
        this.observeNew = observeNew;
    }

    public abstract T getCache();
    public abstract void setCache(T t);
    public abstract boolean isCacheValid();

    public Func1<T, Boolean> isNotNull() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T data) {
                return data != null;
            }
        };
    }

    public Func1<T, Boolean> isNull() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T data) {
                return data == null;
            }
        };
    }

    public Observable<T> observeCache() {
        return Observable.fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return getCache();
            }
        });
    }

    public Observable<T> observeCacheIfValid() {
        return Observable.fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return isCacheValid() ? getCache() : null;
            }
        });
    }

    public Observable<T> observeNewOnly() {
        return observeNew.doOnNext(new Action1<T>() {
            @Override
            public void call(T t) {
                if (t != null) setCache(t);
            }
        });
    }

    public Observable<T> observeNewIfCacheNotValid() {
        return Observable.fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return isCacheValid() ? null : observeNewOnly().toBlocking().first();
            }
        });
    }

    public Observable<T> observeCacheThenNew() {
        //return observeCache().concatWith(observeCacheOrNew());
        return Observable.concat(observeCache(), observeNewIfCacheNotValid().skipWhile(isNull()));
    }

    public Observable<T> observeCacheOrNew() {
        return Observable.concat(observeCacheIfValid(), observeNewOnly()).first(isNotNull());
    }

    public Observable<T> pollNew(final long value, final TimeUnit unit) {
        return observeNewOnly().repeatWhen(interval(value, unit));
    }

    public Func1<Observable<? extends Void>, Observable<?>> interval(final long value, final TimeUnit unit) {
        return new Func1<Observable<? extends Void>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Void> observable) {
                return observable.delay(value, unit);
            }
        };
    }

    public Observable<T> observeCacheThenPollNew(final long value, final TimeUnit unit) {
        return Observable.concat(observeCache(), pollNew(value, unit));
    }

    public Observable<T> observeFirst() {
        return Observable.concat(observeCache(), observeNewOnly()).first(isNotNull());
    }
}
