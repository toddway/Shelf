package com.toddway.shelf;


import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "shelf/src/main/AndroidManifest.xml", constants = BuildConfig.class, emulateSdk = 21)
public class ShelfTest {

    Context context;
    String key = "Test key";
    String stringValue = "Test stringValue";

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        Shelf.init(context);
    }

    @Test
    public void testGetString() throws Exception {
        Shelf.item(key).put(stringValue);

        assertEquals(Shelf.item(key).get(), stringValue);
    }

    @Test
    public void testIsOlderThan() throws Exception {
        Shelf.item(key).put(stringValue);

        TimeUnit.SECONDS.sleep(5);

        assertTrue(Shelf.item(key).isOlderThan(1, TimeUnit.SECONDS));
        assertTrue(!Shelf.item(key).isOlderThan(100, TimeUnit.SECONDS));
    }

    @Test
    public void testGetObject() {
        Pojo pojo = new Pojo();
        pojo.strings = Arrays.asList("one", "two", "three");

        Shelf.item(key).put(pojo);

        pojo = Shelf.item(key).get(Pojo.class);

        assertEquals(pojo.strings.iterator().next(), "one");
        assertEquals(pojo.num, 5);
    }

    @After
    public void tearDown() throws Exception {
        Shelf.clear("");
    }

    private class Pojo {
        Collection<String> strings;
        int num = 5;
    }
}
