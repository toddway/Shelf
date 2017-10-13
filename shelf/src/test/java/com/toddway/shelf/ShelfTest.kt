package com.toddway.shelf


import com.toddway.shelf.serializer.GsonSerializer
import com.toddway.shelf.storage.ClassLoaderStorage
import com.toddway.shelf.storage.FileStorage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class ShelfTest {
    internal lateinit var shelf: Shelf

    @Before
    @Throws(Exception::class)
    fun setUp() {
        shelf = Shelf(
                FileStorage(File("/tmp"),
                        GsonSerializer(),
                        TimeUnit.MINUTES.toMillis(1))
        )

        shelf.clear("")
    }

    @Test
    @Throws(Exception::class)
    fun testGetPojo() {
        val pojo1 = Pojo.create()
        shelf.item(key).put(pojo1)
        val pojo2 = shelf.item(key)[Pojo::class.java]

        assertEquals(pojo1.integer.toLong(), pojo2!!.integer.toLong())
        assertEquals(pojo1.firstFromList.toLong(), pojo2.firstFromList.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testIsOlderThan() {
        val shelfItem = shelf.item(key)
                .put(Pojo.create())
                .maxAge(1, TimeUnit.SECONDS)

        TimeUnit.SECONDS.sleep(2)

        assertTrue(shelfItem.isOlderThanLifetime)
        assertTrue(shelfItem.isOlderThan(1, TimeUnit.SECONDS))
        assertTrue(!shelfItem.isOlderThan(100, TimeUnit.SECONDS))
    }

    @Ignore
    @Test
    @Throws(Exception::class)
    fun testClassLoader() {
        shelf = Shelf(ClassLoaderStorage())
        val pojo = shelf.item("pojo.json")[Pojo::class.java]
        assertEquals(pojo!!.list.iterator().next().toLong(), 1)
    }

    @Test
    @Throws(Exception::class)
    fun testArraysAsList() {
        val pojos1 = Arrays.asList(Pojo.create(), Pojo.create())
        shelf.item(key).put(pojos1)
        val pojos2 = Arrays.asList(*shelf.item(key)[Array<Pojo>::class.java]!!)

        assertEquals(pojos1[0].integer.toLong(), pojos2[0].integer.toLong())
    }

    @Test
    fun testClear() {
        val shelfItem = shelf.item(key).put(Pojo.create())
        assertTrue(shelfItem.exists())

        shelf.item("urelated key").clear()
        assertTrue(shelfItem.exists())

        shelf.item(key).clear()
        assertFalse(shelfItem.exists())
    }

    @Test
    fun testPutNullHasNoErrors() {
        shelf.item(key).clear()
        shelf.item(key).put(null)
        val s = shelf.item(key)[Pojo::class.java]
        val i = 0
    }

    internal class Pojo {
        var list: List<Int> = ArrayList<Int>()
        var integer: Int = 0

        val firstFromList: Int
            get() = list.iterator().next()

        companion object {

            fun create(): Pojo {
                val pojo = Pojo()
                val list = ArrayList<Int>()
                for (i in 0..99998) {
                    list.add(i)
                }
                pojo.list = list
                pojo.integer = 5
                return pojo
            }
        }
    }

    companion object {

        internal var key = "an example key"
    }

}
