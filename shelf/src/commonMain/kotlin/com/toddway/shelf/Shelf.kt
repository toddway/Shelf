package com.toddway.shelf

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

open class Shelf(var storage : Storage, var encoder: Encoder) {
    fun <T> item(key: String, ks : KSerializer<T>) = Item(key, ks)

    companion object : Shelf(DiskStorage(), JsonEncoder())

    class Item<T>(private val key: String, val ks : KSerializer<T>) {
        fun get() : T? = getString()?.let { encoder.decode(it, ks) }
        fun get(maxAge: Long) : T? = if (maxAge > storage.age(key)) get() else null
        fun put(value : T) = storage.put(key, encoder.encode(value, ks))
        fun valueEquals(value : T) : Boolean = getString().equals(encoder.encode(value, ks))
        private fun getString() = try { storage.get(key) } catch (e : Throwable) { null }
    }

    interface Storage {
        fun get(key: String): String?
        fun put(key: String, item: String)
        fun age(key: String) : Long
    }

    interface Encoder {
        fun <T> encode(item : T, ks: KSerializer<T>) : String
        fun <T> decode(string : String, ks: KSerializer<T>) : T
    }
}

expect class DiskStorage() : Shelf.Storage

class JsonEncoder : Shelf.Encoder {
    override fun <T> encode(item: T, ks: KSerializer<T>): String = Json.stringify(ks, item)
    override fun <T> decode(string: String, ks: KSerializer<T>): T = Json.parse(ks, string)
}

fun <T> KSerializer<T>.shelf(key: String) = Shelf.item(key, this)