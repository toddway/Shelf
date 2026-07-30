package com.toddway.shelf

import kotlinx.serialization.InternalSerializationApi
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Counts delegate calls, for verifying the caching serializer decorator. */
private class SpySerializer : Shelf.Serializer {
    var toTypeCalls = 0
    var fromTypeCalls = 0
    var toTypeListCalls = 0

    override fun <T : Any> fromType(value: T): String {
        fromTypeCalls++
        return "json"
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> toType(string: String, klass: KClass<T>): T {
        toTypeCalls++
        return "deserialized" as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T> {
        toTypeListCalls++
        return listOf("deserialized") as List<T>
    }
}

/** A trivial reversible cipher for round-trip tests: `decrypt(encrypt(x)) == x`. */
private class PrefixCipher : Cipher {
    override fun encrypt(plaintext: String): String = "enc:$plaintext"
    override fun decrypt(ciphertext: String): String = ciphertext.removePrefix("enc:")
}

class CachingSerializerTests {

    @Test
    fun toType_is_cached_for_the_same_payload() {
        val spy = SpySerializer()
        val serializer = CachingSerializer(spy)
        val json = "{\"name\":\"test\"}"

        assertEquals("deserialized", serializer.toType(json, String::class))
        assertEquals("deserialized", serializer.toType(json, String::class))

        assertEquals(1, spy.toTypeCalls)
    }

    @Test
    fun toType_calls_delegate_for_different_payloads() {
        val spy = SpySerializer()
        val serializer = CachingSerializer(spy)

        serializer.toType("json1", String::class)
        serializer.toType("json2", String::class)

        assertEquals(2, spy.toTypeCalls)
    }

    @Test
    fun toTypeList_is_cached() {
        val spy = SpySerializer()
        val serializer = CachingSerializer(spy)

        serializer.toTypeList("[]", String::class)
        serializer.toTypeList("[]", String::class)

        assertEquals(1, spy.toTypeListCalls)
    }

    @Test
    fun fromType_is_never_cached() {
        val spy = SpySerializer()
        val serializer = CachingSerializer(spy)

        assertEquals("json", serializer.fromType("test"))
        assertEquals("json", serializer.fromType("test"))
        assertEquals(2, spy.fromTypeCalls)
    }

    @Test
    fun eldest_entry_is_evicted_when_over_capacity() {
        val spy = SpySerializer()
        val serializer = CachingSerializer(spy, capacity = 1)

        serializer.toType("json1", String::class)
        serializer.toType("json2", String::class) // evicts json1
        serializer.toType("json1", String::class) // miss again

        assertEquals(3, spy.toTypeCalls)
    }
}

@OptIn(InternalSerializationApi::class)
class SecureSerializerTests {

    private val serializer = SecureSerializer(
        PrefixCipher(),
        KotlinxSerializer().apply { register(Obj.serializer()) },
    )

    @Test
    fun value_round_trips_through_encryption() {
        val stored = serializer.fromType(Obj(7))
        assertTrue(stored.startsWith("enc:"), "payload should be encrypted at rest")
        assertNotEquals("{\"v\":7}", stored)
        assertEquals(Obj(7), serializer.toType(stored, Obj::class))
    }

    @Test
    fun list_round_trips_through_encryption() {
        val stored = serializer.fromType(listOf(Obj(1), Obj(2)))
        assertTrue(stored.startsWith("enc:"))
        assertEquals(listOf(Obj(1), Obj(2)), serializer.toTypeList(stored, Obj::class))
    }
}
