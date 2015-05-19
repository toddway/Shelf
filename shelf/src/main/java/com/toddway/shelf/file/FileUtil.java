package com.toddway.shelf.file;


import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

public class FileUtil {

    public static File getFile(Context context, String key) {
        File dir = context.getDir("store", Context.MODE_PRIVATE); //Creating an internal dir;
        return new File(dir, key + ".string"); //Getting a file within the dir
    }

    public static String readFileString(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    public static void writeFileString(File file, String fileString) throws IOException {
        file.setLastModified(new Date().getTime());
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(fileString.getBytes());
        outputStream.close();
    }

    public static <T> T readObjectFile(File file, Class<T> type) {
        try {
            ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return type.cast(input.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> void writeObjectFile(File file, T object) {
        try {
            ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            output.writeObject(object);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
