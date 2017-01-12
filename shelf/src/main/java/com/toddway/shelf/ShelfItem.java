package com.toddway.shelf;

import com.toddway.shelf.rx.CacheSubject;
import com.toddway.shelf.rx.ShelfSubjectFactory;
import com.toddway.shelf.storage.Storage;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;


public class ShelfItem {

    private String key;
    private Storage storage;

    private long maxAge;

    public ShelfItem(Storage storage, String key) {
        this.key = key;
        this.storage = storage;
        this.maxAge = storage.defaultMaxAge();
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

    /**
     * @deprecated use maxAge instead
     */
    @Deprecated
    public ShelfItem lifetime(long maxAge) {
        return maxAge(maxAge);
    }

    public ShelfItem maxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * @deprecated use maxAge instead
     */
    @Deprecated
    public ShelfItem lifetime(long value, TimeUnit unit) {
        return lifetime(unit.toMillis(value));
    }

    public ShelfItem maxAge(long value, TimeUnit unit) {
        return maxAge(unit.toMillis(value));
    }

    public boolean isOlderThanLifetime() {
        return isOlderThan(maxAge);
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

    public <T> CacheSubject<T> subject(Class<T> type) {
        return ShelfSubjectFactory.create(this, type);
    }

    public <T> CacheSubject<List<T>> subjectForList(Class<T[]> type) {
        return ShelfSubjectFactory.createForList(this, type);
    }

    public Action1<Object> put() {
        return new Action1<Object>() {

            @Override
            public void call(Object t) {
                put(t);
            }
        };
    }

    /**
     * @deprecated use subject() or subjectForList() instead
     */
    @Deprecated
    public <T> Observable<T> getObservable(final Class<T> type) {
        return new Shelfable<>(null, this, type).observeCache();
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
