package com.toddway.shelf

import kotlinx.serialization.Serializable
import kotlin.test.*


class ShelfTests {

    @Test
    fun `when an object is put, then the stored value is equal to the original`() {
        val value = Pojo(1)
        val item = Shelf.item("aKey", Pojo.serializer())

        item.put(value)

        assertTrue(item.valueEquals(value))
    }

    @Test
    fun `when an object is not put, then the stored value is not equal to the orginial`() {
        val value = Pojo(1)
        val item = Shelf.item("neverPutKey", Pojo.serializer())

        assertFalse(item.valueEquals(value))
    }

    @Test
    fun `when an object changed after it is put, then the stored value is not equal to the original`() {
        val value = Pojo(1)
        val item = Shelf.item("aKey", Pojo.serializer())

        item.put(value)
        value.list[2] = 22

        assertFalse(item.valueEquals(value))
    }

    @Test
    fun `when getting an item that does not exist, then it returns null`() {
        val item = Shelf.item("neverPutKey", Pojo.serializer())

        assertNull(item.get())
    }

    @Test
    fun `when getting an item that exists (was put), then it returns the original value`() {
        val value = Pojo(1)
        val item = Shelf.item("existsKey", Pojo.serializer())

        item.put(value)

        assertNotNull(item.get())
        assertTrue(item.valueEquals(value))
    }

    @Test
    fun `when getting an item that has expired, then it returns null`() {
        val item = Shelf.item("expiredKey", Pojo.serializer())

        item.put(Pojo(1))

        assertNull(item.get(0))
    }

    @Test
    fun `when getting an item that not has expired, then it returns the original value`() {
        val value = Pojo(1)
        val item = Shelf.item("notExpiredKey", Pojo.serializer())

        item.put(value)

        assertNotNull(item.get(99999))
        assertTrue(item.valueEquals(value))
    }
}


@Serializable
open class Pojo(val v : Int) {
    val list = mutableListOf(1,2,v)
    val string = "Asdfasdfasd"
    val nestedObject = NestedObject()
}

@Serializable
data class NestedObject(val map: MutableMap<Int, String> = mutableMapOf(Pair(1, "asdf")))

//class MapStorage : Shelf.Storage {
//    override fun age(key: String): Long = 0
//    override fun get(key: String): String? {
//        println(map[key])
//        return map[key]
//    }
//    override fun put(key: String, item: String) { map[key] = item }
//    private val map : MutableMap<String, String> = mutableMapOf()
//}
