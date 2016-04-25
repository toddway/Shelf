package com.toddway.shelf;

import com.toddway.shelf.storage.Storage;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;


public class ShelfItem {

    private String key;
    private Storage storage;

    private long lifetime;

    public ShelfItem(Storage storage, String key) {
        this.key = key;
        this.storage = storage;
        this.lifetime = storage.defaultLifetime();
    }

    public boolean exists() {
        return storage.contains(key);
    }

    public ShelfItem put(Object object) {
        storage.put(key, object);
        return this;
    }

    public <T> T get(Class<T> type) {
        if (!exists()) return null;
        return storage.get(key, type);
    }

    public ShelfItem lifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public ShelfItem lifetime(long value, TimeUnit unit) {
        return lifetime(unit.toMillis(value));
    }

    public boolean isOlderThanLifetime() {
        return isOlderThan(lifetime);
    }

    public boolean isOlderThan(long value, TimeUnit unit) {
        return isOlderThan(unit.toMillis(value));
    }

    public boolean isOlderThan(long millis) {
        boolean isOlderThan = true;
        if (this.exists()) {
            long thenMillis = storage.lastModified(key);
            long nowMillis = new Date().getTime();
            isOlderThan = thenMillis < (nowMillis - millis);
        }
        return isOlderThan;
    }

    public boolean clear() {
        return storage.delete(key);
    }

    public <T> Shelfable<T> with(Class<T> type, Observable<T> observeNew) {
        return new Shelfable<>(observeNew, this, type);
    }

    public Action1<Object> put() {
        return new Action1<Object>() {

            @Override
            public void call(Object t) {
                put(t);
            }
        };
    }

    public <T> Observable<T> getObservable(final Class<T> type) {
        return with(type, null).observeCache();
    }

    public <T> Observable.Transformer<T, T> cacheThenNew(final Class<T> type) {
        return Shelfable.cacheThenNew(this, type);
    }

    public <T> Observable.Transformer<T, T> cacheOrNew(final Class<T> type) {
        return Shelfable.cacheOrNew(this, type);
    }

    public <T> Observable.Transformer<T, T> newOnly(final Class<T> type) {
        return Shelfable.newOnly(this, type);
    }

    public <T> Observable.Transformer<T, T> pollNew(final Class<T> type, final long value, final TimeUnit unit) {
        return Shelfable.pollNew(this, type, value, unit);
    }

    public <T> Observable.Transformer<T, T> cacheThenPollNew(final Class<T> type, final long value, final TimeUnit unit) {
        return Shelfable.cacheThenPollNew(this, type, value, unit);
    }
}
