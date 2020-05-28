package com.toddway.shelf

import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KClass

open class Shelf(var storage : Storage, var serializer: Serializer, var clock : Clock = Clock()) {
    fun item(key: String) = Item(key, this)
    fun all() = storage.keys().map { item(it) }.toSet()
    fun clear() = all().forEach { it.remove() }

    @ThreadLocal companion object : Shelf(DiskStorage(), KotlinxSerializer())

    class Item(val key: String, val shelf: Shelf) {
        fun <T : Any> get(type : KClass<T>) : T? = getRaw()?.let { shelf.serializer.toType(it, type) }
        fun <T : Any> getList(type : KClass<T>) : List<T>? = getRaw()?.let { shelf.serializer.toTypeList(it, type) }
        fun <T : Any> put(value : T) : T = putRaw(shelf.serializer.fromType(value)).let { return value }
        fun <T : Any> has(value : T) = getRaw().equals(shelf.serializer.fromType(value))
        fun remove() = shelf.storage.remove(key)
        fun age() : Long? = try { shelf.storage.timestamp(key)?.let { shelf.clock.now() - it } } catch (e : Throwable) { null }
        fun getRaw() : String? = try { shelf.storage.get(key) } catch (e : Throwable) { null }
        fun putRaw(string : String) = shelf.storage.put(key, string, shelf.clock.now())
    }

    interface Storage {
        fun get(key: String): String?
        fun put(key: String, value: String, timestamp : Long)
        fun timestamp(key: String) : Long?
        fun keys(): Set<String>
        fun remove(key: String)
    }

    interface Serializer {
        fun <T : Any> fromType(value : T) : String
        fun <T : Any> toType(string : String, klass : KClass<T>) : T
        fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T>
    }
}

expect open class DiskStorage() : Shelf.Storage

expect open class Clock() {
    open fun now() : Long
}

fun Collection<String>.toShelfKeys() = filter { it.endsWith(".shelf") }.map { it.replace(".shelf", "") }.toSet()
fun String.dotShelf() = "$this.shelf"
fun String.dotDate() = "$this.date"

fun Shelf.Item.olderThan(seconds : Long) = age()?.let { it > seconds } ?: true

inline fun <reified T : Any> Shelf.Item.get() : T? = get(T::class)
inline fun <reified T : Any> Shelf.Item.getList() : List<T>? = getList(T::class)
