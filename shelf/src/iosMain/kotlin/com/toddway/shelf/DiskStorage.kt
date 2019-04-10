package com.toddway.shelf

import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSinceNow

actual class DiskStorage : Shelf.Storage {
    override fun age(key: String): Long {
        return (NSDate().timeIntervalSinceNow() - delegate.doubleForKey(dateKey(key))).toLong()
    }

    override fun get(key: String): String? {
        return delegate.stringForKey(key)
    }

    override fun put(key: String, item: String) {
        delegate.setObject(item, key)
        delegate.setObject(NSDate().timeIntervalSinceNow(), dateKey(key))
    }

    private fun dateKey(key : String) = "$key shelfDate"

    private val delegate: NSUserDefaults = NSUserDefaults.standardUserDefaults()
}