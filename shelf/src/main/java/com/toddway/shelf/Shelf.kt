package com.toddway.shelf

import com.toddway.shelf.storage.FileStorage
import com.toddway.shelf.storage.Storage
import java.io.File
import java.util.*

class Shelf(private val storage: Storage) {
    private var map: WeakHashMap<String, Any>? = null

    constructor(dir: File) : this(FileStorage(dir)) {}

    fun item(key: String): ShelfItem {
        val isUsingMap = map != null
        val isInMap = isUsingMap && map!!.containsKey(key)

        val item = if (isInMap) map!![key] as ShelfItem else ShelfItem(storage, key)
        if (isUsingMap && !isInMap) map!!.put(key, item)
        return item
    }

    fun keys(prefix: String): List<String> {
        return storage.keys(prefix)
    }

    fun clear(prefix: String) {
        for (key in keys(prefix)) {
            item(key).clear()
            if (map != null) map!!.remove(key)
        }
    }

    fun useWeakMap(useWeakMap: Boolean) {
        if (useWeakMap)
            map = WeakHashMap()
        else
            map = null
    }
}
