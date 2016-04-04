package com.toddway.shelf;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GsonFileStorage extends FileStorage {

    public GsonFileStorage(File dir) {
        super(dir);
    }

    @Override
    protected  <T> T deserialize(File file, Class<T> type) {
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            return new Gson().fromJson(reader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected <T> void serialize(File file, T object) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            new Gson().toJson(object, object.getClass(), writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected <T> List<T> deserializeList(File file, Class<T> type)  {
        try {
            List<T> list = new ArrayList<>();
            JsonReader reader = new JsonReader(new FileReader(file));
            reader.beginArray();
            while (reader.hasNext()) {
                T object = new Gson().fromJson(reader, type);
                list.add(object);
            }
            reader.endArray();
            reader.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
