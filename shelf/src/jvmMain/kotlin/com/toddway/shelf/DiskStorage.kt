package com.toddway.shelf

import java.io.File
import java.util.*

actual class DiskStorage : Shelf.Storage {
    override fun age(key: String): Long {
        return Date().time - file(key).lastModified()
    }

    override fun get(key: String): String? {
        return file(key).readText()
    }

    override fun put(key: String, item: String) {
        file(key).writeText(item)
    }

    private fun file(key : String) = File(File("/tmp"), "$key.json")
}