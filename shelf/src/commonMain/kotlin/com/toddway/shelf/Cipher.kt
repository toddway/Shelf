package com.toddway.shelf

/**
 * A minimal symmetric cipher abstraction used by [SecureSerializer] to encrypt stored payloads.
 *
 * Shelf intentionally does not implement any cryptography itself. Consumers provide an
 * implementation adapting their platform's crypto (e.g. Android Keystore-backed AES, a KMP crypto
 * library, etc.):
 *
 * ```kotlin
 * class MyCipher(private val crypto: MyCrypto) : Cipher {
 *     override fun encrypt(plaintext: String) = crypto.encrypt(plaintext)
 *     override fun decrypt(ciphertext: String) = crypto.decrypt(ciphertext)
 * }
 * ```
 *
 * Implementations must satisfy `decrypt(encrypt(x)) == x` for any string `x`.
 */
interface Cipher {
    fun encrypt(plaintext: String): String
    fun decrypt(ciphertext: String): String
}
