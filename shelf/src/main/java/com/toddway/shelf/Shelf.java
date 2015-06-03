package com.toddway.shelf;

import java.io.File;
import java.util.List;
import java.util.WeakHashMap;

public class Shelf {

    private Storage storage;
    private WeakHashMap<String, Object> items;

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

}
