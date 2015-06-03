package com.toddway.shelf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileStorage implements Storage {

    File dir;
    static String EXT = ".obj";

    public FileStorage(File dir) {
        this.dir = dir;
        dir.mkdir();
    }

    protected File file(String key) {
        return new File(dir, key + EXT); //Getting a file within the dir
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        if (!file(key).exists()) return null;
        return deserialize(file(key), type);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> type) {
        if (!file(key).exists()) return null;
        return deserializeList(file(key), type);
    }

    @Override
    public synchronized <T> void put(String key, T value) {
        serialize(file(key), value);
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

    protected <T> T deserialize(File file, Class<T> type)  {
        try {
            ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return type.cast(input.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected <T> void serialize(File file, T object) {
        try {
            ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            output.writeObject(object);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected <T> List<T> deserializeList(File file, Class<T> type) {
        return deserialize(file, List.class);
    }
}