package com.toddway.shelf.storage;

import com.toddway.shelf.ShelfUtils;
import com.toddway.shelf.serializer.GsonSerializer;
import com.toddway.shelf.serializer.JavaSerializer;
import com.toddway.shelf.serializer.Serializer;

import java.io.InputStream;
import java.util.List;

/**
 * Created by tway on 4/7/16.
 */
public class ClassLoaderStorage implements Storage {

    ClassLoader classLoader;
    Serializer serializer;

    public ClassLoaderStorage() {
        this.classLoader = getClass().getClassLoader();
        this.serializer = ShelfUtils.hasGsonOnClasspath() ? new GsonSerializer() : new JavaSerializer();
    }

    public ClassLoaderStorage(Serializer serializer) {
        this();
        this.serializer = serializer;
    }

    protected InputStream inputStream(String key) {
        return classLoader.getResourceAsStream(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return serializer.deserialize(inputStream(key), type);
    }

    @Override
    public <T> void put(String key, T value) {
        //read only
    }

    @Override
    public boolean delete(String key) {
        //read only
        return false;
    }

    @Override
    public boolean contains(String key) {
        return inputStream(key) != null;
    }

    @Override
    public long lastModified(String key) {
        return 0;
    }

    @Override
    public List<String> keys(String startsWith) {
        return null;
    }
}
