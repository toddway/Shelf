package com.toddway.shelf.file;


import com.toddway.shelf.Storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileStorage implements Storage {

    File dir;

    public FileStorage(File dir) {
        this.dir = dir;
    }

    private File getFile(String key) {
        return new File(dir, key + ".string"); //Getting a file within the dir
    }

    @Override
    public String get(String key) throws IOException {
        if (getFile(key).exists()) {
            return FileUtil.readFileString(getFile(key));
        } else return null;
    }

    @Override
    public synchronized boolean put(String key, String value) throws IOException {
        FileUtil.writeFileString(getFile(key), value);
        return true;
    }

    @Override
    public boolean delete(String key) throws IOException {
        return getFile(key).delete();
    }

    @Override
    public boolean contains(String key) {
        return getFile(key).exists();
    }

    @Override
    public long lastModified(String key) {
        return getFile(key).lastModified();
    }

    @Override
    public List<String> keys(String prefix) {
        File[] files = dir.listFiles();
        List<String> keys = new ArrayList<>();
        for (int f = 0; f < files.length; f++) {
            String filename = files[f].getName();
            if (filename.startsWith(prefix)) {
                keys.add(filename.substring(0,filename.lastIndexOf(".")));
            }
        }
        return keys;
    }
}