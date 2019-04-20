package com.toddway.shelf


import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.defaultSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlin.collections.set
import kotlin.reflect.KClass


class KotlinxJsonSerializer(val json: Json = Json.plain) : Shelf.Serializer {

    override fun <T : Any> serialize(value: T): String {
        return json.stringify(findByValue(value), value)
    }

    override fun <T : Any> deserialize(string: String, klass: KClass<T>): T {
        return json.parse(findByClass(klass), string)
    }

    override fun <T : Any> deserializeList(string: String, klass: KClass<T>): List<T> {
        return json.parse(findByClass(klass).list, string)
    }

    private val serializers: MutableMap<KClass<*>, KSerializer<*>> = mutableMapOf()

    fun register(klass: KClass<*>, serializer: KSerializer<*>) {
        serializers[klass] = serializer
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> findByValue(value: T): KSerializer<T> {
        return if (value is List<*>) {
            value.find { it != null }?.let { findByClass(it::class).list }
        } else {
            findByClass(value::class)
        } as KSerializer<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> findByClass(klass: KClass<T>): KSerializer<T> {
        return serializers[klass]?.let { return it as KSerializer<T> }
            ?: klass.defaultSerializer()
            ?: throw SerializationException("No registered serializer for: $klass.  Use KotlinxJsonSerializer.register() to add one")
    }
}


