package com.toddway.shelf;


import com.toddway.shelf.serializer.GsonSerializer;
import com.toddway.shelf.storage.ClassLoaderStorage;
import com.toddway.shelf.storage.FileStorage;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShelfTest {

    static String key = "an example key";
    Shelf shelf;

    @Before
    public void setUp() throws Exception {
        shelf = new Shelf(
                    new FileStorage(new File("/tmp"),
                    new GsonSerializer(),
                    TimeUnit.MINUTES.toMillis(1))
                );

        shelf.clear("");
    }

    @Test
    public void testGetPojo() throws Exception {
        Pojo pojo1 = Pojo.create();
        shelf.item(key).put(pojo1);
        Pojo pojo2 = shelf.item(key).get(Pojo.class);

        assertEquals(pojo1.integer, pojo2.integer);
        assertEquals(pojo1.getFirstFromList(), pojo2.getFirstFromList());
    }

    @Test
    public void testIsOlderThan() throws Exception {
        ShelfItem shelfItem = shelf.item(key)
                        .put(Pojo.create())
                        .lifetime(1, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(2);

        assertTrue(shelfItem.isOlderThanLifetime());
        assertTrue(shelfItem.isOlderThan(1, TimeUnit.SECONDS));
        assertTrue(!shelfItem.isOlderThan(100, TimeUnit.SECONDS));
    }

    @Ignore
    @Test
    public void testClassLoader() throws Exception {
        shelf = new Shelf(new ClassLoaderStorage());
        Pojo pojo = shelf.item("pojo.json").get(Pojo.class);
        assertEquals((int) pojo.list.iterator().next(), 1);
    }

    @Test
    public void testArraysAsList() throws Exception {
        List<Pojo> pojos1 = Arrays.asList(Pojo.create(), Pojo.create());
        shelf.item(key).put(pojos1);
        List<Pojo> pojos2 = Arrays.asList(shelf.item(key).get(Pojo[].class));

        assertEquals(pojos1.get(0).integer, pojos2.get(0).integer);
    }

    @Test
    public void testClear() {
        ShelfItem shelfItem = shelf.item(key).put(Pojo.create());
        assertTrue(shelfItem.exists());

        shelf.item("urelated key").clear();
        assertTrue(shelfItem.exists());

        shelf.item(key).clear();
        assertFalse(shelfItem.exists());
    }

    @Test public void testPutNullHasNoErrors() {
        shelf.item(key).clear();
        shelf.item(key).put(null);
        Pojo s = shelf.item(key).get(Pojo.class);
        int i = 0;
    }

    static class Pojo {
        public List<Integer> list;
        public int integer;

        public int getFirstFromList() {
            return list.iterator().next();
        }

        static Pojo create() {
            Pojo pojo = new Pojo();
            ArrayList<Integer> list = new ArrayList();
            for (int i = 0; i < 99999; i++) {
                list.add(i);
            }
            pojo.list = list;
            pojo.integer = 5;
            return pojo;
        }
    }

}
