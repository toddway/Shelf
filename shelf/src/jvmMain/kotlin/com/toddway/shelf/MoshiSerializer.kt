package com.toddway.shelf

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.RuntimeException
import kotlin.reflect.KClass

class MoshiSerializer(private val moshi : Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()) :
    Shelf.Serializer<String> {

    override fun <T : Any> fromType(value: T): String {
        return when (value) {
            is String -> return value
            is List<*> -> value.find { it != null }
                ?.let { moshi.adapter<List<*>>(listType(it.javaClass)).toJson(value) }
                ?: throw RuntimeException("Moshi toJson for List returned null")
            else -> moshi.adapter(value.javaClass).toJson(value)
        }
    }

    override fun <T : Any> toType(string: String, klass: KClass<T>): T {
        return moshi.adapter(klass.java).fromJson(string)
            ?: throw RuntimeException("Moshi fromJson returned null")
    }

    override fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T> {
        return moshi.adapter<List<T>>(listType(klass.java)).fromJson(string)
            ?: throw RuntimeException("Moshi fromJson returned null")
    }

    private fun <T : Any> listType(type : Class<T>) =
        Types.newParameterizedType(List::class.java, type)
}
