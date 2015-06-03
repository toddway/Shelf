package com.toddway.shelf;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ShelfItem<T> {

    private String key;
    private Storage storage;
    private T item;

    public ShelfItem(Storage storage, String key) {
        this.key = key;
        this.storage = storage;
    }

    public boolean exists() {
        return storage.contains(key);
    }

    public void put(T item) {
        this.item = item;
        storage.put(key, item);
    }

    public <T> T get(Class<T> type) {
        if (item != null) return (T) item;
        if (!exists()) return null;
        return storage.get(key, type);
    }

    public <T> List<T> getListOf(Class<T> type) {
        if (item != null) return (List<T>) item;
        if (!exists()) return null;
        return storage.getList(key, type);
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
}
