package com.toddway.shelf;

import android.content.Context;

import com.toddway.shelf.file.FileStorage;

import java.util.List;
import java.util.WeakHashMap;

public class Shelf {

    private Storage storage;
    private WeakHashMap<String, Object> items;

    public Shelf(Storage storage) {
        this(storage, false);
    }

    public Shelf(Storage storage, boolean useWeakMap) {
        this.storage = storage;
        if (useWeakMap) items = new WeakHashMap<>();
    }

    public <T> ShelfItem<T> getItem(String key) {
        if (items != null && items.containsKey(key)) {
            return (ShelfItem<T>) items.get(key);
        } else {
            ShelfItem<T> item = new ShelfItem<>(storage, key);
            if (items != null) items.put(key, item);
            return item;
        }
    }

    public List<String> getKeys(String prefix) {
        return storage.keys(prefix);
    }

    public void clearAll(String prefix) {
        for (String key : getKeys(prefix)) {
            getItem(key).clear();
            if (items != null) items.remove(key);
        }
    }

    // ---- STATIC ---- //

    private static Shelf SHELF;
    private static String SHELF_NAME = "shelf";

    public static synchronized void init(Context context) {
        SHELF = new Shelf(new FileStorage(context.getDir(SHELF_NAME, Context.MODE_PRIVATE))); //create in an internal dir
    }

    public static synchronized void init(Storage storage) {
        SHELF = new Shelf(storage);
    }

    public static <T> ShelfItem<T> item(String key) {
        return SHELF.getItem(key);
    }

    public static List<String> keys(String prefix) {
        return SHELF.getKeys(prefix);
    }

    public static void clear(String prefix) {
        SHELF.clearAll(prefix);
    }

}
