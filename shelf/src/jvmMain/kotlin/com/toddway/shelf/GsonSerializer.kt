package com.toddway.shelf

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.reflect.KClass

class GsonSerializer(val gson : Gson = Gson()) : Shelf.Serializer {
    override fun <T : Any> fromType(value: T): String {
        return gson.toJson(value)
    }

    override fun <T : Any> toType(string: String, klass: KClass<T>): T {
        return gson.fromJson(string, klass.java)
    }

    override fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T> {
        return gson.fromJson<List<T>>(string, listType(klass.java))
    }

    private fun <T : Any> listType(type : Class<T>) =
        TypeToken.getParameterized(List::class.java, type).type
}