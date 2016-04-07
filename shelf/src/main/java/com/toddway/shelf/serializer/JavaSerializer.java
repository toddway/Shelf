package com.toddway.shelf.serializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by tway on 4/7/16.
 */
public class JavaSerializer implements Serializer {

    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) {
        try {
            ObjectInput input = new ObjectInputStream(new BufferedInputStream(inputStream));
            return type.cast(input.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> void serialize(OutputStream outputStream, T object) {
        try {
            ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(outputStream));
            output.writeObject(object);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
