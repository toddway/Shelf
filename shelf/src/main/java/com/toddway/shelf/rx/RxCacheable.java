package com.toddway.shelf.rx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;

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

    public Predicate<T> isNotNull() {
        return new Predicate<T>() {
            @Override
            public boolean test(@NonNull T data) throws Exception {
                return data != null;
            }
        };
    }

    public Predicate<T> isNull() {
        return new Predicate<T>() {
            @Override
            public boolean test(T data) {
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
        return observeNew.doOnNext(new Consumer<T>() {
            @Override
            public void accept(T t) {
                if (t != null) setCache(t);
            }
        });
    }

    public Observable<T> observeNewIfCacheNotValid() {
        return Observable.fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return isCacheValid() ? null : observeNewOnly().blockingFirst();
            }
        });
    }

    public Observable<T> observeCacheThenNew() {
        //return observeCache().concatWith(observeCacheOrNew());
        return Observable.concat(observeCache(), observeNewIfCacheNotValid().skipWhile(isNull()));
    }

    public Maybe<T> observeCacheOrNew() {
        return Observable.concat(observeCacheIfValid(), observeNewOnly()).filter(isNotNull()).firstElement();
    }

    public Observable<T> pollNew(final long value, final TimeUnit unit) {
        return observeNewOnly().repeatWhen(interval(value, unit));
    }

    public Function<Observable<?>, ObservableSource<?>> interval(final long value, final TimeUnit unit) {
        return observable -> observable.delay(value, unit);
    }

    public Observable<T> observeCacheThenPollNew(final long value, final TimeUnit unit) {
        return Observable.concat(observeCache(), pollNew(value, unit));
    }

    public Maybe<T> observeFirst() {
        return Observable.concat(observeCache(), observeNewOnly()).filter(isNotNull()).firstElement();
    }
}
