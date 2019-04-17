package com.toddway.shelf

import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.dumps
import kotlinx.serialization.json.Json
import kotlinx.serialization.loads
import kotlin.native.concurrent.ThreadLocal

open class Shelf(var storage : Storage = DiskStorage(), var encoder: Encoder = JsonEncoder(), var clock : Clock = Clock()) {
    fun item(key: String) = Item(key)
    fun all() = storage.keys().map { item(it) }.toSet()
    fun clear() = all().forEach { it.remove() }

    @ThreadLocal companion object : Shelf()

    class Item(val key: String) {
        fun <T> get(type : KSerializer<T>) : T? = raw()?.let { encoder.decode(it, type) }
        fun <T> get(type : KSerializer<T>, maxAge: Long) : T? = if (maxAge > age()) get(type) else null
        fun <T> put(type : KSerializer<T>, value : T) : Item = storage.put(key, encoder.encode(value, type), clock.now()).let { return this }
        fun <T> has(type : KSerializer<T>, value : T) = raw().equals(encoder.encode(value, type))
        fun remove() = storage.remove(key)
        fun age() = storage.timestamp(key)?.let { clock.now() - it } ?: 0
        private fun raw() = try { storage.get(key) } catch (e : Throwable) { null }
    }

    interface Storage {
        fun get(key: String): String?
        fun put(key: String, value: String, timestamp : Long)
        fun timestamp(key: String) : Long?
        fun keys(): Set<String>
        fun remove(key: String)
    }

    interface Encoder {
        fun <T> encode(value : T, type: KSerializer<T>) : String
        fun <T> decode(string : String, type: KSerializer<T>) : T
    }
}

expect open class DiskStorage() : Shelf.Storage

expect open class Clock() {
    open fun now() : Long
}

class JsonEncoder : Shelf.Encoder {
    override fun <T> encode(value: T, type: KSerializer<T>): String = Json.stringify(type, value)
    override fun <T> decode(string: String, type: KSerializer<T>): T = Json.parse(type, string)
}

class CborEncoder : Shelf.Encoder {
    override fun <T> encode(value: T, type: KSerializer<T>): String = Cbor.dumps(type, value)
    override fun <T> decode(string: String, type: KSerializer<T>): T = Cbor.loads(type, string)
}

fun Collection<String>.toShelfKeys() = filter { it.endsWith(".shelf") }.map { it.replace(".shelf", "") }.toSet()
fun String.dotShelf() = "$this.shelf"
fun String.dotDate() = "$this.date"
