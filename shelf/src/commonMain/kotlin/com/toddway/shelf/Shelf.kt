package com.toddway.shelf

import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KClass

open class Shelf(var storage : Storage = DiskStorage(), var serializer: Serializer = KotlinxJsonSerializer(), var clock : Clock = Clock()) {
    fun item(key: String) = Item(key)
    fun all() = storage.keys().map { item(it) }.toSet()
    fun clear() = all().forEach { it.remove() }

    @ThreadLocal companion object : Shelf()

    class Item(val key: String) {
        fun <T : Any> get(type : KClass<T>) : T? = raw()?.let { serializer.deserialize(it, type) }
        fun <T : Any> getList(type : KClass<T>) : List<T>? = raw()?.let { serializer.deserializeList(it, type) }
        fun <T : Any> put(value : T) : T = storage.put(key, serializer.serialize(value), clock.now()).let { return value }
        fun <T : Any> has(value : T) = raw().equals(serializer.serialize(value))
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
        fun <T : Any> serialize(value : T) : String
        fun <T : Any> deserialize(string : String, klass : KClass<T>) : T
        fun <T : Any> deserializeList(string: String, klass: KClass<T>): List<T>
    }
}

expect open class DiskStorage() : Shelf.Storage

expect open class Clock() {
    open fun now() : Long
}

fun Collection<String>.toShelfKeys() = filter { it.endsWith(".shelf") }.map { it.replace(".shelf", "") }.toSet()
fun String.dotShelf() = "$this.shelf"
fun String.dotDate() = "$this.date"

fun Shelf.Item.ageAtLeast(seconds: Long) = age()?.atLeast(seconds)?.let { this }
fun Shelf.Item.ageAtMost(seconds: Long) = age()?.atMost(seconds)?.let { this }
fun Long.atMost(max: Long) : Long? = takeIf { this <= max }
fun Long.atLeast(min: Long) : Long? = takeIf { this >= min }

