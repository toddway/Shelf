package com.toddway.shelf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A [Shelf.Storage] that counts delegate calls, for verifying the caching decorator. */
private class SpyStorage : Shelf.Storage {
    val backing = MemoryStorage()
    var getCalls = 0
    var timestampCalls = 0

    override fun get(key: String): String? {
        getCalls++
        return backing.get(key)
    }

    override fun put(key: String, value: String, timestamp: Long) = backing.put(key, value, timestamp)
    override fun remove(key: String) = backing.remove(key)
    override fun keys(): Set<String> = backing.keys()

    override fun timestamp(key: String): Long? {
        timestampCalls++
        return backing.timestamp(key)
    }
}

class MemoryStorageTests {
    private val storage = MemoryStorage()

    @Test
    fun put_then_get_returns_value() {
        storage.put("k", "v", 10L)
        assertEquals("v", storage.get("k"))
        assertEquals(10L, storage.timestamp("k"))
        assertEquals(setOf("k"), storage.keys())
    }

    @Test
    fun remove_clears_value_and_timestamp() {
        storage.put("k", "v", 10L)
        storage.remove("k")
        assertNull(storage.get("k"))
        assertNull(storage.timestamp("k"))
        assertEquals(emptySet(), storage.keys())
    }

    @Test
    fun keys_are_a_defensive_copy() {
        storage.put("k", "v", 0L)
        val keys = storage.keys()
        storage.put("k2", "v2", 0L) // must not mutate the previously returned set
        assertEquals(setOf("k"), keys)
    }
}

class ThreadSafeStorageTests {
    private val storage = ThreadSafeStorage(MemoryStorage())

    @Test
    fun delegates_all_operations() {
        storage.put("k", "v", 5L)
        assertEquals("v", storage.get("k"))
        assertEquals(5L, storage.timestamp("k"))
        assertEquals(setOf("k"), storage.keys())
        storage.remove("k")
        assertNull(storage.get("k"))
    }
}

class CachingStorageTests {

    @Test
    fun repeated_get_hits_delegate_only_once() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy)
        spy.put("k", "v", 0L)

        assertEquals("v", caching.get("k"))
        assertEquals("v", caching.get("k"))
        assertEquals("v", caching.get("k"))

        assertEquals(1, spy.getCalls)
    }

    @Test
    fun put_populates_cache_so_get_skips_delegate() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy)

        caching.put("k", "v", 0L)

        assertEquals("v", caching.get("k"))
        assertEquals(0, spy.getCalls)
    }

    @Test
    fun timestamp_is_cached_after_get() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy)
        spy.put("k", "v", 123L)

        caching.get("k")
        assertEquals(123L, caching.timestamp("k"))
        assertEquals(1, spy.timestampCalls) // only the read-through during get()
    }

    @Test
    fun timestamp_falls_through_to_delegate_on_miss() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy)
        spy.put("k", "v", 123L)

        assertEquals(123L, caching.timestamp("k"))
        assertEquals(1, spy.timestampCalls)
    }

    @Test
    fun remove_clears_cache() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy)
        caching.put("k", "v", 0L)

        caching.remove("k")
        assertNull(caching.get("k"))
    }

    @Test
    fun keys_delegates() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy)
        caching.put("k1", "v1", 0L)
        assertEquals(setOf("k1"), caching.keys())
    }

    @Test
    fun eldest_entry_is_evicted_when_over_capacity() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy, capacity = 1)

        caching.put("k1", "v1", 0L)
        caching.put("k2", "v2", 0L) // evicts k1 from the memory cache

        assertEquals("v1", caching.get("k1")) // forces a read-through
        assertEquals(1, spy.getCalls)
    }

    @Test
    fun get_returns_null_when_delegate_has_nothing() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy)
        assertNull(caching.get("missing"))
    }

    @Test
    fun most_recently_used_entry_survives_eviction() {
        val spy = SpyStorage()
        val caching = CachingStorage(spy, capacity = 2)
        caching.put("k1", "v1", 0L)
        caching.put("k2", "v2", 0L)

        caching.get("k1")             // k1 becomes most-recently-used
        caching.put("k3", "v3", 0L)   // should evict k2, not k1

        assertEquals("v1", caching.get("k1"))
        assertEquals(0, spy.getCalls) // k1 still cached
        assertTrue(spy.timestampCalls >= 0)
    }
}
