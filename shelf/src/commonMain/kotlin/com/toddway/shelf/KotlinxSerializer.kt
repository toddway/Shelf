package com.toddway.shelf

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.collections.set
import kotlin.reflect.KClass

class KotlinxSerializer(private val json: Json = Json.nonstrict) : Shelf.Serializer {

    override fun <T : Any> fromType(value: T): String {
        return if (value is String) value
        else json.stringify(findByValue(value), value)
    }

    override fun <T : Any> toType(string: String, klass: KClass<T>): T {
        return json.parse(findByClass(klass), string)
    }

    override fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T> {
        return json.parse(findByClass(klass).list, string)
    }

    private val serializers: MutableMap<KClass<*>, KSerializer<*>> = mutableMapOf()

    fun register(klass: KClass<*>, serializer: KSerializer<*>) {
        serializers[klass] = serializer
    }

    inline fun <reified T: Any> register(serializer: KSerializer<T>) = register(T::class, serializer)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> findByValue(value: T): KSerializer<T> {
        return if (value is List<*>) {
            value.find { it != null }?.let { findByClass(it::class).list }
        } else {
            findByClass(value::class)
        } as KSerializer<T>
    }

    @OptIn(ImplicitReflectionSerializer::class)
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> findByClass(klass: KClass<T>): KSerializer<T> {

        if (klass == List::class || klass == ArrayList::class)
            throw RuntimeException("For top-level Lists, use Shelf.Item.getList() instead of Shelf.Item.get()")

        return serializers[klass]?.let { return it as KSerializer<T> }
            ?: klass.serializerOrNull()
            ?: throw RuntimeException("No serializer for: $klass.  Use KotlinxSerializer.register() to add one")
    }
}
