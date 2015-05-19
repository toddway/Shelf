package com.toddway.shelf;


import java.io.IOException;
import java.util.List;

public interface Storage {

    public String get(String key) throws IOException;

    public boolean put(String key, String value) throws IOException;

    public boolean delete(String key) throws IOException;

    public boolean contains(String key);

    public long lastModified(String key);

    public List<String> keys(String startsWith);
}
