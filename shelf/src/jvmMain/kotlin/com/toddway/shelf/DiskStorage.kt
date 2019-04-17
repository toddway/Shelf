package com.toddway.shelf

import java.io.File
import java.util.*

actual open class DiskStorage : Shelf.Storage, FileStorage()

open class FileStorage(private val delegate : File = File("/tmp")) : Shelf.Storage {
    init {
        delegate.mkdir()
    }

    override fun remove(key: String) {
        delegate.item(key).delete()
    }

    override fun keys(): Set<String> {
        return delegate.list().toSet().toShelfKeys()
    }

    override fun timestamp(key: String): Long? {
        return delegate.item(key).lastModified().let { if (it == 0L) null else it / 1000 }
    }

    override fun get(key: String): String? {
        return delegate.item(key).readText()
    }

    override fun put(key: String, value: String, timestamp: Long) {
        delegate.item(key).writeText(value)
        delegate.item(key).setLastModified(timestamp * 1000)
    }
}

fun File.item(key : String) = File(this, key.dotShelf())

actual open class Clock {
    actual open fun now(): Long = (Date().time / 1000)
}
