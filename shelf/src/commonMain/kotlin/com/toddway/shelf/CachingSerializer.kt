package com.toddway.shelf

import kotlin.reflect.KClass

/**
 * Caches the result of deserialization ([toType] / [toTypeList]) in a bounded LRU, so repeated
 * reads of the same stored payload skip re-parsing. Writes ([fromType]) are not cached.
 *
 * ## Contract: immutable types only
 * Cached objects are returned **by shared reference**. If a caller mutates a returned object, every
 * later read of the same payload observes that mutation, corrupting the cache. Use this decorator
 * only with immutable models (e.g. `data class`es whose fields are `val` and immutable).
 *
 * ## Cache key
 * Entries are keyed on the **full payload string** plus the target [KClass]. Keying on the whole
 * payload (rather than its hash code) means there is no chance of a hash collision returning the
 * wrong object, and a changed value naturally becomes a new key — no manual invalidation needed.
 * The [KClass] is used directly (not its `qualifiedName`, which is unavailable on Kotlin/JS and
 * Native), matching how [KotlinxSerializer] keys its serializer registry.
 */
class CachingSerializer(
    private val delegate: Shelf.Serializer,
    private val capacity: Int = 50,
) : Shelf.Serializer {

    private data class Key(val klass: KClass<*>, val list: Boolean, val string: String)

    private val lock = Lock()
    private val cache = LinkedHashMap<Key, Any>()

    private fun cached(key: Key): Any? = lock.withLock {
        val value = cache.remove(key) ?: return@withLock null
        cache[key] = value // move to most-recently-used
        value
    }

    private fun store(key: Key, value: Any) = lock.withLock {
        cache.remove(key)
        cache[key] = value
        while (cache.size > capacity) {
            cache.remove(cache.keys.iterator().next())
        }
    }

    override fun <T : Any> fromType(value: T): String = delegate.fromType(value)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> toType(string: String, klass: KClass<T>): T {
        val key = Key(klass, list = false, string = string)
        cached(key)?.let { return it as T }
        val value = delegate.toType(string, klass)
        store(key, value)
        return value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T> {
        val key = Key(klass, list = true, string = string)
        cached(key)?.let { return it as List<T> }
        val value = delegate.toTypeList(string, klass)
        store(key, value)
        return value
    }
}
