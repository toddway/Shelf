package com.toddway.shelf

/**
 * Makes any [Shelf.Storage] safe for concurrent use by serializing every operation behind a
 * single reentrant lock.
 *
 * The primary use is wrapping [FileStorage]/[DiskStorage], which are not safe for concurrent
 * access to the same key (a read can observe a half-written file). A single lock — rather than a
 * read/write lock — is deliberate: it is truly multiplatform, and in the recommended stack a
 * [CachingStorage] sits in front and serves most reads from memory, so lock contention on the
 * underlying storage is rare.
 */
class ThreadSafeStorage(private val delegate: Shelf.Storage) : Shelf.Storage {
    private val lock = Lock()

    override fun get(key: String): String? = lock.withLock { delegate.get(key) }

    override fun put(key: String, value: String, timestamp: Long): Unit =
        lock.withLock { delegate.put(key, value, timestamp) }

    override fun remove(key: String): Unit = lock.withLock { delegate.remove(key) }

    override fun keys(): Set<String> = lock.withLock { delegate.keys() }

    override fun timestamp(key: String): Long? = lock.withLock { delegate.timestamp(key) }
}
