package com.toddway.shelf;


import java.io.IOException;
import java.util.List;

public interface Storage {

    <T> T get(String key, Class<T> type);

    <T> List<T> getList(String key, Class<T> type);

    <T> void put(String key, T value);

    boolean delete(String key);

    boolean contains(String key);

    long lastModified(String key);

    List<String> keys(String startsWith);
}
