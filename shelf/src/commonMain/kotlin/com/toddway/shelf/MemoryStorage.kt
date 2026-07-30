package com.toddway.shelf

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * In-memory [Shelf.Storage]. Values live only for the lifetime of the process.
 *
 * Useful as a base for tests (fast and hermetic, unlike hitting the filesystem) and as the
 * innermost storage for non-persistent caches. Thread-safe: a bare map would throw
 * [ConcurrentModificationException] if iterated while mutated from another thread.
 */
class MemoryStorage : Shelf.Storage {
    private val lock = SynchronizedObject()
    private val values = mutableMapOf<String, String>()
    private val timestamps = mutableMapOf<String, Long>()

    override fun get(key: String): String? = synchronized(lock) { values[key] }

    override fun put(key: String, value: String, timestamp: Long): Unit = synchronized(lock) {
        values[key] = value
        timestamps[key] = timestamp
    }

    override fun remove(key: String): Unit = synchronized(lock) {
        values.remove(key)
        timestamps.remove(key)
    }

    // Defensive copy so callers can iterate without racing concurrent mutations.
    override fun keys(): Set<String> = synchronized(lock) { values.keys.toSet() }

    override fun timestamp(key: String): Long? = synchronized(lock) { timestamps[key] }
}
