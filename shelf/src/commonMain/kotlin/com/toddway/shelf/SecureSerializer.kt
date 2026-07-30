package com.toddway.shelf

import kotlin.reflect.KClass

/**
 * Encrypts serialized payloads at rest by wrapping another [Shelf.Serializer] with a [Cipher].
 *
 * On write, a value is first serialized by [delegate] and the resulting string is encrypted. On
 * read, the stored string is decrypted and then deserialized. The [Cipher] never sees your model
 * types — only their serialized string form — so any serializer can be secured this way:
 *
 * ```kotlin
 * val serializer = CachingSerializer(SecureSerializer(myCipher, KotlinxSerializer()))
 * ```
 */
class SecureSerializer(
    private val cipher: Cipher,
    private val delegate: Shelf.Serializer,
) : Shelf.Serializer {

    override fun <T : Any> fromType(value: T): String =
        cipher.encrypt(delegate.fromType(value))

    override fun <T : Any> toType(string: String, klass: KClass<T>): T =
        delegate.toType(cipher.decrypt(string), klass)

    override fun <T : Any> toTypeList(string: String, klass: KClass<T>): List<T> =
        delegate.toTypeList(cipher.decrypt(string), klass)
}
