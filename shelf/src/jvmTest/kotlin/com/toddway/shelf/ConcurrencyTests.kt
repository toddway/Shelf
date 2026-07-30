package com.toddway.shelf

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Stresses the concurrency decorators under real parallelism (JVM only — common tests can't
 * assert genuine multithreading). A bare [MemoryStorage] map would throw
 * ConcurrentModificationException here; the point is that the wrapped stack does not.
 */
class ConcurrencyTests {

    @Test
    fun thread_safe_caching_storage_survives_concurrent_access() {
        val storage = ThreadSafeStorage(CachingStorage(MemoryStorage(), capacity = 64))
        val threads = 16
        val opsPerThread = 2_000
        val pool = Executors.newFixedThreadPool(threads)

        repeat(threads) { t ->
            pool.submit {
                repeat(opsPerThread) { i ->
                    val key = "key-${(t * 31 + i) % 128}"
                    storage.put(key, "v$i", i.toLong())
                    storage.get(key)
                    storage.timestamp(key)
                    storage.keys()
                }
            }
        }
        pool.shutdown()
        assertEquals(true, pool.awaitTermination(30, TimeUnit.SECONDS), "workers did not finish")

        // Every key written should be readable and consistent.
        storage.keys().forEach { key ->
            assertNotNull(storage.get(key), "value missing for $key")
            assertNotNull(storage.timestamp(key), "timestamp missing for $key")
        }
    }
}
