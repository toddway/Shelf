package com.toddway.shelf;


import com.toddway.shelf.serializer.JacksonSerializer;
import com.toddway.shelf.storage.ClassLoaderStorage;
import com.toddway.shelf.storage.FileStorage;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShelfTest {

    String key = "item key";
    String string = "item value";
    Shelf shelf;

    @Before
    public void setUp() throws Exception {
        shelf = new Shelf(new FileStorage(new File("/tmp"), new JacksonSerializer()));
        shelf.clear("");
    }

    @Test
    public void testGetString() throws Exception {
        shelf.item(key).put(string);
        assertEquals(shelf.item(key).get(String.class), string);
    }

    @Test
    public void testIsOlderThan() throws Exception {
        shelf.item(key).put(string);
        TimeUnit.SECONDS.sleep(5);
        assertTrue(shelf.item(key).isOlderThan(1, TimeUnit.SECONDS));
        assertTrue(!shelf.item(key).isOlderThan(100, TimeUnit.SECONDS));
    }

    @Test
    public void testGetPojo() throws Exception{
        Pojo pojo = Pojo.create();

        long start = System.currentTimeMillis();
        shelf.item(key).put(pojo);
        System.out.println("put time:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        pojo = shelf.item(key).get(Pojo.class);
        System.out.println("get time:" + (System.currentTimeMillis() - start));

        Integer i = pojo.integerArrayList.iterator().next();
        System.out.println("first item: " + i);
        assertEquals((int) i, 0);
        assertEquals(pojo.num, 5);
    }

    @Test
    public void testClassLoader() throws Exception {
        shelf = new Shelf(new ClassLoaderStorage());
        Pojo pojo = shelf.item("pojo.json").get(Pojo.class);
        assertEquals((int) pojo.integerArrayList.iterator().next(), 1);
    }

    @Test
    public void testArray() throws Exception {
        List<Pojo> list = Arrays.asList(Pojo.create(), Pojo.create());
        shelf.item(key).put(list.toArray());
        list = Arrays.asList(shelf.item(key).get(Pojo[].class));
        assertEquals((int) list.get(0).integerArrayList.iterator().next(), 0);
    }

    static class Pojo {
        public ArrayList<Integer> integerArrayList;
        public int num;

        static Pojo create() {
            Pojo pojo = new Pojo();
            ArrayList<Integer> list = new ArrayList();
            for (int i = 0; i < 99999; i++) {
                list.add(i);
            }
            pojo.integerArrayList = list;
            pojo.num = 5;
            return pojo;
        }
    }

}
