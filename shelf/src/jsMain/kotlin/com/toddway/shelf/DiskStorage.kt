package com.toddway.shelf

import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.browser.localStorage
import kotlin.js.Date

actual open class DiskStorage : Shelf.Storage, LocalStorage()

open class LocalStorage(private val delegate : Storage = localStorage) : Shelf.Storage {

    override fun get(key: String): String? {
        return delegate[key.dotShelf()]
    }

    override fun put(key: String, value: String, timestamp: Long) {
        delegate[key.dotShelf()] = value
        delegate[key.dotDate()] = timestamp.toString()
    }

    override fun timestamp(key: String): Long? {
        return delegate[key.dotDate()]?.toLong()
    }

    override fun keys(): Set<String> {
        val keys = mutableSetOf<String>()
        for (i in 0..delegate.length) { delegate.key(i)?.let { keys.add(it) } }
        return keys.toShelfKeys()
    }

    override fun remove(key: String) {
        delegate.removeItem(key.dotShelf())
        delegate.removeItem(key.dotDate())
    }
}

actual open class Clock {
    actual open fun now() = Date.now().toLong() / 1000
}

