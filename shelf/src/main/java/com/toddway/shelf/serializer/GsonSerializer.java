package com.toddway.shelf.serializer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by tway on 4/7/16.
 */
public class GsonSerializer implements Serializer {

    Gson gson;

    public GsonSerializer() {
        gson = new Gson();
    }

    public GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) {
        return gson.fromJson(new JsonReader(new InputStreamReader(inputStream)), type);
    }

    @Override
    public <T> void serialize(OutputStream outputStream, T object) {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream));
        gson.toJson(object, object.getClass(), writer);
        try {
            writer.close();
        } catch (IOException e) {

        }
    }
}
