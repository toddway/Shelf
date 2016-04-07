package com.toddway.shelf;

import com.toddway.shelf.storage.FileStorage;
import com.toddway.shelf.storage.Storage;

import java.io.File;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class Shelf {

    private Storage storage;
    private WeakHashMap<String, Object> items;
    long maxAge = TimeUnit.MINUTES.toMillis(1);

    public Shelf(File dir) {
        this(new FileStorage(dir));
    }

    public Shelf(Storage storage) {
        this.storage = storage;
    }

    public <T> ShelfItem<T> item(String key) {
        if (items != null && items.containsKey(key)) {
            return (ShelfItem<T>) items.get(key);
        } else {
            ShelfItem<T> item = new ShelfItem<>(storage, key);
            if (items != null) items.put(key, item);
            return item;
        }
    }

    public List<String> keys(String prefix) {
        return storage.keys(prefix);
    }

    public void clear(String prefix) {
        for (String key : keys(prefix)) {
            item(key).clear();
            if (items != null) items.remove(key);
        }
    }

    public void useWeakMap(boolean useWeakMap) {
        if (useWeakMap) items = new WeakHashMap<>();
        else items = null;
    }

    public Shelf setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public <T> Cacheable<T> cacheable(String name, Class<T> type, Observable<T> observeNew) {
        Cacheable c = new Cacheable<>(observeNew);
        c.shelf = this;
        c.maxAge = maxAge;
        c.name = name;
        c.type = type;
        return c;
    }

    public static class Cacheable<T> extends RxCacheable<T> {
        Shelf shelf;
        String name;
        Class<T> type;
        long maxAge;

        public Cacheable(Observable<T> observeNew) {
            super(observeNew);
            ShelfUtils.checkRx();
        }

        public ShelfItem<T> item() {
            return shelf.item(name);
        }

        @Override
        public T getCache() {
            return shelf.item(name).get(type);
        }

        @Override
        public boolean isCacheValid() {
            return !item().isOlderThan(maxAge);
        }

        @Override
        public void setCache(T t) {
            item().put(t);
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

}
