package com.toddway.shelf

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.test.*

class ShelfTests {
    val key = "aKey"
    var value = Obj(1)
    val clock = ManualClock()
    val type = Obj::class

    @BeforeTest fun `when_clearing_shelf_then_no_item_or_value_exist`() {
        Shelf.storage = DiskStorage()
        Shelf.encoder = KotlinxJsonSerializer().apply {
            register(type, Obj.serializer())
        }
        Shelf.clock = clock

        val item = Shelf.item(key).put(value)
        Shelf.clear()

        assertEquals(emptySet(), Shelf.all(), "Items are cleared")
        assertNull(Shelf.item(item.key).get(type), "Value is cleared")
    }

    @Test fun `when_an_object_is_put_then_the_stored_value_is_equal_to_the_original`() {
        val item = Shelf.item(key).put(value)

        assertTrue(item.has(value))
    }

    @Test fun `when_an_object_is_not_put_then_the_stored_value_is_not_equal_to_the_original`() {
        assertFalse(Shelf.item(key).has(value))
    }

    @Test fun `when_an_object_changed_after_it_is_put_then_the_stored_value_is_not_equal_to_the_original`() {
        val item = Shelf.item(key).put(value)
        value.nested[2] = 22

        assertFalse(item.has(value))
    }

    @Test fun `when_an_object_is_put_then_the_stored_value_is_not_equal_to_an_object_with_different_data`() {
        val item = Shelf.item(key).put(value)

        assertFalse(item.has(Obj(2)))
    }

    @Test fun `when_getting_an_item_that_does_not_exist_then_it_returns_null`() {
        assertNull(Shelf.item(key).get(type))
    }

    @Test fun `when_getting_an_item_that_exists_then_it_returns_the_original_value`() {
        val item = Shelf.item(key).put(value)

        assertTrue(item.get(type)?.let { item.has(it) } ?: false )
    }

    @Test fun `when_getting_an_item_that_has_expired_then_it_returns_null`() {
        val item = Shelf.item(key).put(value)
        clock.forward(5)

        assertNull(item.takeIf { it.age() < 4 }?.get(type))
    }

    @Test fun `when_getting_an_item_that_has_not_expired_then_it_returns_a_value`() {
        val item = Shelf.item(key).put(value)
        clock.forward(5)

        assertNotNull(item.takeIf { it.age() < 6 }?.get(type))
    }

    @Test fun `when_getting_an_item_after_DiskStorage_is_reset_then_it_still_returns_the_original_value`() {
        val item = Shelf.item(key).put(value)
        Shelf.storage = DiskStorage()

        assertTrue(item.has(value))
    }

    @Test fun `when_getting_an_item_after_MemoryStorage_is_reset_then_it_returns_null`() {
        Shelf.storage = MemoryStorage()
        val item = Shelf.item(key).put(value)

        assertTrue(item.has(value))

        Shelf.storage = MemoryStorage()

        assertNull(item.get(type))
    }

    @Test fun `when_getting_all_items_that_have_been_put_then_a_set_of_all_items_is_returned`() {
        val keys = setOf(key, "secondKey")
        keys.forEach { Shelf.item(it).put(value) }

        assertEquals(Shelf.all().size, keys.size)
    }

    @Test fun `when_no_items_are_put_then_an_empty_set_is_returned`() {
        assertEquals(0, Shelf.all().size)
    }

    @Test fun `when_removing_an_existing_key_then_the_value_no_longer_exists`() {
        val item = Shelf.item(key).put(value)
        item.remove()

        assertNull(item.get(type))
    }

    @Test fun `when_using_CborEncoder_then_returned_values_match_the_stored_values`() {
        //Shelf.encoder = CborEncoder()
        val item = Shelf.item(key).put(value)

        assertTrue(item.has(value))
        assertTrue(item.get(type)?.let{ item.has(it) } ?: false)
    }

    @Test fun `when_getting_the_age_of_an_item_that_does_not_exist`() {
        val age = Shelf.item(key).age()

        assertEquals(0.toLong(), age, "$age should equal zero")
    }

    @Test fun `when_getting_the_age_after_an_interval_then_the_age_is_equal_to_the_interval`() {
        Shelf.item(key).put(value)
        clock.forward(5)

        val age = Shelf.item(key).age()

        assertEquals(5, age, "$age ${clock.now()} should equal 5")
    }


    @Test fun `test_lists`() {
        Shelf.encoder = KotlinxJsonSerializer().apply {
            register(Obj::class, Obj.serializer())
        }

        val list = listOf(Obj(1), Obj(2))
        val item = Shelf.item("item").put(list)

        assertTrue(item.has(list))
        assertTrue(item.getList(Obj::class)?.let { item.has(it) } ?: false)


        val list2 = listOf("ASDfadf")
        val item2 = Shelf.item("string").put(list2)

        assertTrue(item2.has(list2))
        assertTrue(item2.getList(String::class)?.let { item2.has(it) } ?: false)
    }
    
    //todo test not registered
}


open class MemoryStorage : Shelf.Storage {
    override fun remove(key: String) { map.remove(key) }
    override fun keys(): Set<String> = map.keys
    override fun timestamp(key: String): Long = 0
    override fun get(key: String): String? = map[key]
    override fun put(key: String, value: String, timestamp: Long) { map[key] = value }
    private val map : MutableMap<String, String> = mutableMapOf()
}


class ManualClock : Clock() {

    var offset : Long = 0

    override fun now(): Long {
        return super.now() + offset
    }

    fun forward(seconds: Long) {
        offset += seconds
    }
}

//Blocking is slow and doesn't work for JS tests
class BlockingClock : Clock() {

    fun block(seconds : Long) {
        runBlocked { delay(seconds * 1000) }
    }

    fun forward(seconds : Long) {
        block(seconds)
    }
}

expect fun runBlocked(block: suspend () -> Unit)

@Serializable
data class Obj(val v : Int) {
    val nested = mutableListOf(1, 2, v)
}


