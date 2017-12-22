package com.toddway.shelf

import com.toddway.shelf.storage.Storage

import java.util.Date
import java.util.concurrent.TimeUnit


class ShelfItem(private val storage: Storage, private val key: String) {

    private var maxAge: Long = 0

    val isOlderThanLifetime: Boolean
        get() = isOlderThan(maxAge)

    init {
        this.maxAge = storage.defaultMaxAge()
    }

    fun exists(): Boolean {
        return storage.contains(key)
    }

    fun put(`object`: Any?): ShelfItem {
        if (`object` == null)
            clear()
        else
            storage.put(key, `object`)
        return this
    }

    operator fun <T> get(type: Class<T>): T? {
        return if (!exists()) null else storage.get(key, type)
    }


    @Deprecated("use maxAge instead")
    fun lifetime(maxAge: Long): ShelfItem {
        return maxAge(maxAge)
    }

    fun maxAge(maxAge: Long): ShelfItem {
        this.maxAge = maxAge
        return this
    }

    @Deprecated("use maxAge instead")
    fun lifetime(value: Long, unit: TimeUnit): ShelfItem {
        @Suppress("DEPRECATION")
        return lifetime(unit.toMillis(value))
    }

    fun maxAge(value: Long, unit: TimeUnit): ShelfItem {
        return maxAge(unit.toMillis(value))
    }

    fun isOlderThan(value: Long, unit: TimeUnit): Boolean {
        return isOlderThan(unit.toMillis(value))
    }

    fun isOlderThan(millis: Long): Boolean {
        var isOlderThan = true
        if (this.exists()) {
            val thenMillis = storage.lastModified(key)
            val nowMillis = Date().time
            isOlderThan = thenMillis < nowMillis - millis
        }
        return isOlderThan
    }

    fun clear(): Boolean {
        return storage.delete(key)
    }
}
