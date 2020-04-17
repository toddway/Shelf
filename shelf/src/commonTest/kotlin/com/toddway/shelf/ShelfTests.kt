package com.toddway.shelf

import kotlinx.serialization.Serializable
import kotlin.test.*

@Suppress("TooManyFunctions")
class ShelfTests {
    val key = "aKey"
    var value = Obj(1)
    val clock = ManualClock()
    val shelf = Shelf(
        DiskStorage(),
        KotlinxSerializer().apply { register(Obj.serializer()) },
        clock
    )

    @BeforeTest
    fun `when_clearing_shelf_then_no_item_or_value_exist`() {
        with(shelf.item(key)) {
            put(value)
            shelf.clear()

            assertEquals(emptySet(), shelf.all(), "Items are cleared")
            assertNull(get(), "Value is cleared")
        }
    }

    @Test
    fun `when_an_object_is_put_then_the_stored_value_is_equal_to_the_original`() {
        with(shelf.item(key)) {
            put(value)

            assertTrue(has(value))
        }
    }

    @Test
    fun `when_an_object_is_not_put_then_the_stored_value_is_not_equal_to_the_original`() {
        with(shelf.item(key)) {
            assertFalse(has(value))
        }
    }

    @Test
    fun `when_an_object_changed_after_it_is_put_then_the_stored_value_is_not_equal_to_the_original`() {
        with(shelf.item(key)) {
            put(value)
            value.nested[2] = 22

            assertFalse(has(value))
        }
    }

    @Test
    fun `when_an_object_is_put_then_the_stored_value_is_not_equal_to_an_object_with_different_data`() {
        with(shelf.item(key)) {
            put(value)

            assertFalse(has(Obj(2)))
        }
    }

    @Test
    fun `when_getting_an_item_that_does_not_exist_then_it_returns_null`() {
        with(shelf.item(key)) {
            assertNull(get())

        }
    }

    @Test
    fun `when_getting_an_item_that_exists_then_it_returns_the_original_value`() {
        with(shelf.item(key)) {
            put(value)

            assertTrue(has(value))
            assertTrue(get<Obj>()?.let { has(it) } ?: false)
        }
    }

    @Test
    fun `when_getting_an_item_that_has_expired_then_it_returns_null`() {
        with(shelf.item(key)) {
            put(value)
            clock.forward(5)

            assertNull(takeIf { it.age()!! < 4 }?.get())
        }
    }

    @Test
    fun `when_getting_an_item_that_has_not_expired_then_it_returns_a_value`() {
        with(shelf.item(key)) {
            put(value)
            clock.forward(5)

            assertNotNull(takeIf { it.age()!! < 6 }?.get<Obj>())
        }
    }

    @Test
    fun `when_getting_an_item_after_DiskStorage_is_reset_then_it_still_returns_the_original_value`() {
        with(shelf.item(key)) {
            put(value)
            shelf.storage = DiskStorage()

            assertTrue(has(value))
        }
    }

    @Test
    fun `when_getting_an_item_after_MemoryStorage_is_reset_then_it_returns_null`() {
        with(shelf.item(key)) {
            shelf.storage = MemoryStorage()
            put(value)

            assertTrue(has(value))

            shelf.storage = MemoryStorage()

            assertNull(get())
        }
    }

    @Test
    fun `when_getting_all_items_that_have_been_put_then_a_set_of_all_items_is_returned`() {
        with(shelf.item(key)) {
            val keys = setOf(key, "secondKey")
            keys.forEach { shelf.item(it).put(value) }

            assertEquals(shelf.all().size, keys.size)
        }
    }

    @Test
    fun `when_no_items_are_put_then_an_empty_set_is_returned`() {
        assertEquals(0, shelf.all().size)
    }

    @Test
    fun `when_removing_an_existing_key_then_the_value_no_longer_exists`() {
        with(shelf.item(key)) {
            put(value)
            remove()

            assertNull(get())
        }
    }

    @Test
    fun `when_getting_the_age_of_an_item_that_does_not_exist`() {
        with(shelf.item(key)) {
            val age = age()

            assertNull(age, "$age should be null")
        }
    }

    @Test
    fun `when_getting_the_age_after_an_interval_then_the_age_is_equal_to_the_interval`() {
        with(shelf.item(key)) {
            put(value)
            clock.forward(5)

            val age = age()

            assertEquals(5, age, "$age ${clock.now()} should equal 5")
        }
    }


    @Test
    fun `test_lists`() {
        shelf.serializer = KotlinxSerializer().apply {
            register(Obj.serializer())
        }

        with(shelf.item(key)) {
            val list = listOf(Obj(1), Obj(2))
            put(list)

            assertTrue(has(list))
            assertTrue(getList<Obj>()?.let { has(it) } ?: false)

            val list2 = listOf("ASDfadf")
            put(list2)

            assertTrue(has(list2))
            assertTrue(getList<String>()?.let { has(it) } ?: false)
        }
    }
}

open class MemoryStorage : Shelf.Storage {
    override fun remove(key: String) {
        map.remove(key)
    }

    override fun keys(): Set<String> = map.keys
    override fun timestamp(key: String): Long = 0
    override fun get(key: String): String? = map[key]
    override fun put(key: String, value: String, timestamp: Long) {
        map[key] = value
    }

    private val map: MutableMap<String, String> = mutableMapOf()
}

class ManualClock : Clock() {

    var offset: Long = 0

    override fun now(): Long {
        return super.now() + offset
    }

    fun forward(seconds: Long) {
        offset += seconds
    }
}

@Serializable
data class Obj(val v: Int) {
    val nested = mutableListOf(1, 2, v)
}

////Blocking is slow and doesn't work for JS tests
//class BlockingClock : Clock() {
//
//    fun block(seconds: Long) {
//        runBlocked { delay(seconds * 1000) }
//    }
//
//    fun forward(seconds: Long) {
//        block(seconds)
//    }
//}
//
//expect fun runBlocked(block: suspend () -> Unit)
