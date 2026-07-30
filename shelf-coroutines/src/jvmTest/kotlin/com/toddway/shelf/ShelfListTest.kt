package com.toddway.shelf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

/** Self-contained serializer for `String` / `List<String>`, so the test needs no serialization dep. */
private object StringListSerializer : Shelf.Serializer {
    private const val SEP = " "

    override fun <T : Any> fromType(value: T): String =
        if (value is List<*>) value.joinToString(SEP) { it.toString() } else value.toString()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> toType(string: String, klass: KClass<T>): T = string as T

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T> =
        if (string.isEmpty()) emptyList() else string.split(SEP) as List<T>
}

class ShelfListTest {

    private fun shelfList() = ShelfList(
        Shelf(MemoryStorage(), StringListSerializer),
        key = "test-list",
        type = String::class,
        dispatcher = Dispatchers.Unconfined,
    )

    @Test
    fun load_populates_flow_from_storage() = runBlocking {
        val list = shelfList()
        list.shelf.item("test-list").put(listOf("a", "b"))

        list.load()

        assertEquals(listOf("a", "b"), list.flow.value)
    }

    @Test
    fun add_persists_and_publishes() = runBlocking {
        val list = shelfList()

        list.add("item")

        assertEquals(listOf("item"), list.flow.value)
        assertEquals(listOf("item"), list.shelf.item("test-list").getList(String::class))
    }

    @Test
    fun remove_persists_and_publishes() = runBlocking {
        val list = shelfList()
        list.set(listOf("a", "b"))

        list.remove("a")

        assertEquals(listOf("b"), list.flow.value)
        assertEquals(listOf("b"), list.shelf.item("test-list").getList(String::class))
    }

    @Test
    fun load_on_empty_storage_yields_empty_list() = runBlocking {
        val list = shelfList()

        list.load()

        assertEquals(emptyList(), list.flow.value)
    }

    @Test
    fun set_replaces_and_publishes() = runBlocking {
        val list = shelfList()

        list.set(listOf("x"))

        assertEquals(listOf("x"), list.flow.value)
    }
}
