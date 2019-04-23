package com.toddway.shelf

import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KClass

open class Shelf(var storage : Storage = DiskStorage(), var serializer: Serializer = KotlinxJsonSerializer(), var clock : Clock = Clock()) {
    fun item(key: String) = Item(key)
    fun all() = storage.keys().map { item(it) }.toSet()
    fun clear() = all().forEach { it.remove() }

    @ThreadLocal companion object : Shelf()

    class Item(val key: String) {
        fun <T : Any> get(type : KClass<T>) : T? = raw()?.let { serializer.read(it, type) }
        fun <T : Any> getList(type : KClass<T>) : List<T>? = raw()?.let { serializer.readList(it, type) }
        fun <T : Any> put(value : T) : T = storage.put(key, serializer.write(value), clock.now()).let { return value }
        fun <T : Any> has(value : T) = raw().equals(serializer.write(value))
        fun remove() = storage.remove(key)
        fun age() : Long? = try { storage.timestamp(key)?.let { clock.now() - it } } catch (e : Throwable) { null }
        private fun raw() : String? = try { storage.get(key) } catch (e : Throwable) { null }
    }

    interface Storage {
        fun get(key: String): String?
        fun put(key: String, value: String, timestamp : Long)
        fun timestamp(key: String) : Long?
        fun keys(): Set<String>
        fun remove(key: String)
    }

    interface Serializer {
        fun <T : Any> write(value : T) : String
        fun <T : Any> read(string : String, klass : KClass<T>) : T
        fun <T : Any> readList(string: String, klass: KClass<T>): List<T>
    }
}

expect open class DiskStorage() : Shelf.Storage

expect open class Clock() {
    open fun now() : Long
}

fun Collection<String>.toShelfKeys() = filter { it.endsWith(".shelf") }.map { it.replace(".shelf", "") }.toSet()
fun String.dotShelf() = "$this.shelf"
fun String.dotDate() = "$this.date"

fun Long?.isGreaterThan(other : Long) = this?.let { it > other } ?: false
fun Long?.isLessThan(other : Long) = this?.let { it < other } ?: false

inline fun <reified T : Any> Shelf.Item.get() = get(T::class)
inline fun <reified T : Any> Shelf.Item.getList() = getList(T::class)