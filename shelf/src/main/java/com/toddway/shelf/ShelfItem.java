package com.toddway.shelf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ShelfItem<T> {

    private String key;
    private Type type;
    private Storage storage;
    private Object item;

    public ShelfItem(Storage storage, String key) {
        this.key = key;
        this.storage = storage;
        this.type = new TypeToken<T>(){}.getType();
    }

    private ShelfItem<T> setType(Type type) {
        this.type = type;
        this.item = null;
        return this;
    }

    public boolean exists() {
        return storage.contains(key);
    }

    public boolean put(T item) {
        this.item = item;
        boolean result = false;
        try {
            String json = getGson().toJson(item);
            storage.put(key, json);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public <T> T get(Type type) {
        setType(type);
        if (item == null) {
            try {
                String json = storage.get(key);
                item = getGson().fromJson(json, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (T) item;
    }

    private Gson getGson() {
        return new Gson();
    }

    public <T> T get(TypeToken token) {
        return get(token.getType());
    }

    public <T> T get() {
        return get(type);
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
        boolean result = false;
        try {
            item = null;
            result = storage.delete(key);
        } catch (Exception e) {}
        return result;
    }
}
