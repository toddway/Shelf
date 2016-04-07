package com.toddway.shelf.serializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tway on 4/7/16.
 */
public interface Serializer {
    <T> T deserialize(InputStream inputStream, Class<T> type);
    <T> void serialize(OutputStream outputStream, T object);
}
