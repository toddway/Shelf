package com.toddway.shelf

/**
 * A read-through LRU cache in front of another [Shelf.Storage].
 *
 * Raw string values and their timestamps are cached in memory; the least-recently-used entry is
 * evicted once [capacity] is exceeded. This avoids re-reading disk (or any slow delegate) for hot
 * keys. Thread-safe.
 *
 * The eviction order is maintained in a plain insertion-ordered [LinkedHashMap] by removing and
 * re-inserting an entry whenever it is accessed. This reproduces the behavior of the JVM
 * access-order `LinkedHashMap` constructor without depending on a JVM-only API, keeping the
 * decorator multiplatform.
 */
class CachingStorage(
    private val delegate: Shelf.Storage,
    private val capacity: Int = 100,
) : Shelf.Storage {

    private data class Entry(val value: String, val timestamp: Long)

    private val lock = Lock()
    private val cache = LinkedHashMap<String, Entry>()

    // Must be called while holding [lock]. Moves the entry to the most-recently-used position.
    private fun touch(key: String): Entry? {
        val entry = cache.remove(key) ?: return null
        cache[key] = entry
        return entry
    }

    // Must be called while holding [lock]. Inserts as most-recently-used and evicts if over capacity.
    private fun store(key: String, entry: Entry) {
        cache.remove(key)
        cache[key] = entry
        while (cache.size > capacity) {
            cache.remove(cache.keys.iterator().next())
        }
    }

    override fun get(key: String): String? {
        lock.withLock { touch(key) }?.let { return it.value }
        // Miss: read through outside the lock so a slow delegate doesn't block other keys.
        val value = delegate.get(key) ?: return null
        val entry = Entry(value, delegate.timestamp(key) ?: 0L)
        lock.withLock { store(key, entry) }
        return entry.value
    }

    override fun put(key: String, value: String, timestamp: Long) {
        delegate.put(key, value, timestamp)
        lock.withLock { store(key, Entry(value, timestamp)) }
    }

    override fun remove(key: String) {
        delegate.remove(key)
        lock.withLock { cache.remove(key) }
    }

    // Delegate is the source of truth for the full key set.
    override fun keys(): Set<String> = delegate.keys()

    override fun timestamp(key: String): Long? {
        lock.withLock { touch(key) }?.let { return it.timestamp }
        return delegate.timestamp(key)
    }
}
