package com.toddway.shelf;

import com.toddway.shelf.storage.Storage;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import rx.Observable;


public class ShelfItem<T> {

    private String key;
    private Storage storage;
    private T item;
    private long lifetime;

    public ShelfItem(Storage storage, String key) {
        this.key = key;
        this.storage = storage;
        this.lifetime = storage.defaultLifetime();
    }

    public boolean exists() {
        return storage.contains(key);
    }

    public ShelfItem<T> put(T t) {
        this.item = t;
        storage.put(key, t);
        return this;
    }

    public <T> T get(Class<T> type) {
        if (item != null) return (T) item;
        if (!exists()) return null;
        return storage.get(key, type);
    }

    public ShelfItem<T> setLifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
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
        item = null;
        return storage.delete(key);
    }

    public <T> Policies<T> policies(Class<T> type, Observable<T> observeNew) {
        Policies c = new Policies<>(observeNew);
        c.item = this;
        c.type = type;
        return c;
    }

    public static class Policies<T> extends CachePolicies<T> {
        ShelfItem<T> item;
        Class<T> type;

        public Policies(Observable<T> observeNew) {
            super(observeNew);
            ShelfUtils.checkRx();
        }

        public ShelfItem<T> item() {
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
    }
}
