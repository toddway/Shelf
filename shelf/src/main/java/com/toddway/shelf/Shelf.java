package com.toddway.shelf;

import com.toddway.shelf.storage.FileStorage;
import com.toddway.shelf.storage.Storage;

import java.io.File;
import java.util.List;
import java.util.WeakHashMap;

public class Shelf {

    private Storage storage;
    private WeakHashMap<String, Object> map;

    public Shelf(File dir) {
        this(new FileStorage(dir));
    }

    public Shelf(Storage storage) {
        this.storage = storage;
    }

    public ShelfItem item(String key) {
        boolean isUsingMap = map != null;
        boolean isInMap = isUsingMap && map.containsKey(key);

        ShelfItem item = isInMap ? (ShelfItem) map.get(key) : new ShelfItem(storage, key);
        if (isUsingMap && !isInMap) map.put(key, item);
        return item;
    }

    public List<String> keys(String prefix) {
        return storage.keys(prefix);
    }

    public void clear(String prefix) {
        for (String key : keys(prefix)) {
            item(key).clear();
            if (map != null) map.remove(key);
        }
    }

    public void useWeakMap(boolean useWeakMap) {
        if (useWeakMap) map = new WeakHashMap<>();
        else map = null;
    }
}
