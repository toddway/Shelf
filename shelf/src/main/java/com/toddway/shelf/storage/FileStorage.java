package com.toddway.shelf.storage;

import com.toddway.shelf.ShelfUtils;
import com.toddway.shelf.serializer.Serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileStorage implements Storage {

    File dir;
    static String EXT = ".obj";
    Serializer serializer;
    long defaultLifetime;

    public FileStorage(File dir) {
        this(dir, ShelfUtils.defaultSerializer(), ShelfUtils.defaultLifetime());
    }

    public FileStorage(File dir, Serializer serializer, long defaultLifetime) {
        this.dir = dir;
        this.serializer = serializer;
        this.defaultLifetime = defaultLifetime;

        if (dir != null) dir.mkdir();
    }

    protected File file(String key) {
        return new File(dir, key + EXT); //Getting a file within the dir
    }

    protected FileInputStream inputStream(String key) {
        try {
            return new FileInputStream(file(key));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    protected FileOutputStream outputStream(String key) {
        try {
            return new FileOutputStream(file(key));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        if (!file(key).exists()) return null;
        return serializer.deserialize(inputStream(key), type);
    }

    @Override
    public synchronized <T> void put(String key, T value) {
        serializer.serialize(outputStream(key), value);
    }

    @Override
    public boolean delete(String key) {
        return file(key).delete();
    }

    @Override
    public boolean contains(String key) {
        return file(key).exists();
    }

    @Override
    public long lastModified(String key) {
        return file(key).lastModified();
    }

    @Override
    public long defaultLifetime() {
        return defaultLifetime;
    }

    @Override
    public List<String> keys(String prefix) {
        File[] files = dir.listFiles();
        List<String> keys = new ArrayList<>();
        for (int f = 0; f < files.length; f++) {
            String filename = files[f].getName();
            int doxIndex = filename.lastIndexOf(".");
            if (filename.startsWith(prefix) && doxIndex > 0) {
                keys.add(filename.substring(0,doxIndex));
            }
        }
        return keys;
    }
}