package com.toddway.shelf

import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSince1970

actual open class DiskStorage : Shelf.Storage<String>, UserDefaultsStorage()

open class UserDefaultsStorage(private val delegate: NSUserDefaults = NSUserDefaults.standardUserDefaults()) : Shelf.Storage<String> {
    override fun remove(key: String) {
        delegate.removeObjectForKey(key.dotShelf())
        delegate.removeObjectForKey(key.dotDate())
    }

    override fun keys(): Set<String> {
        return delegate.dictionaryRepresentation().keys.map { it.toString() }.toShelfKeys()
    }

    override fun timestamp(key: String): Long? {
        return delegate.stringForKey(key.dotDate())?.toLong()
    }

    override fun get(key: String): String? {
        return delegate.stringForKey(key.dotShelf())
    }

    override fun put(key: String, value: String, timestamp: Long) {
        delegate.setObject(value, key.dotShelf())
        delegate.setObject(timestamp.toString(), key.dotDate())
    }
}

actual open class Clock actual constructor() {
    actual open fun now() = NSDate().timeIntervalSince1970.toLong()
}
