package com.toddway.shelf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toddway.shelf.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tway on 4/7/16.
 */
public class JacksonSerializer implements Serializer {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) {
        try {
            return mapper.readValue(inputStream, type);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public <T> void serialize(OutputStream outputStream, T object) {
        try {
            mapper.writeValue(outputStream, object);
        } catch (IOException e) {

        }
    }
}
