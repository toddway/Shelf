package com.toddway.shelf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A trivial reversible cipher for round-trip tests: `decrypt(encrypt(x)) == x`. */
private class EncPrefixCipher : Cipher {
    override fun encrypt(plaintext: String): String = "enc:$plaintext"
    override fun decrypt(ciphertext: String): String = ciphertext.removePrefix("enc:")
}

class NamespacedStorageTests {

    @Test
    fun keys_are_scoped_to_the_namespace_and_stripped() {
        val backing = MemoryStorage()
        val users = NamespacedStorage(backing, "user")
        val apps = NamespacedStorage(backing, "app")

        users.put("a", "1", 0L)
        apps.put("b", "2", 0L)

        assertEquals(setOf("a"), users.keys())
        assertEquals(setOf("b"), apps.keys())
        // physically distinct keys in the shared backing store
        assertEquals(setOf("user.a", "app.b"), backing.keys())
    }

    @Test
    fun get_and_remove_are_scoped() {
        val backing = MemoryStorage()
        val users = NamespacedStorage(backing, "user")
        val apps = NamespacedStorage(backing, "app")
        users.put("k", "u", 0L)
        apps.put("k", "a", 0L)

        assertEquals("u", users.get("k"))
        assertEquals("a", apps.get("k"))

        users.remove("k")
        assertNull(users.get("k"))
        assertEquals("a", apps.get("k")) // other namespace untouched
    }

    @Test
    fun blank_name_is_rejected() {
        assertFailsWith<IllegalArgumentException> { NamespacedStorage(MemoryStorage(), " ") }
    }

    @Test
    fun name_containing_the_separator_is_rejected() {
        assertFailsWith<IllegalArgumentException> { NamespacedStorage(MemoryStorage(), "a.b") }
    }
}

class FastShelfTests {

    @Test
    fun value_round_trips() {
        val shelf = fastShelf(MemoryStorage())
        shelf.item("n").put(42)
        assertEquals(42, shelf.item("n").get<Int>())
    }

    @Test
    fun named_shelves_over_one_store_are_isolated() {
        val backing = MemoryStorage()
        val user = fastShelf("user", backing)
        val app = fastShelf("app", backing)

        user.item("count").put(1)
        app.item("count").put(2)

        assertEquals(1, user.item("count").get<Int>())
        assertEquals(2, app.item("count").get<Int>())

        user.clear()
        assertNull(user.item("count").get<Int>())
        assertEquals(2, app.item("count").get<Int>()) // clear() is scoped to the namespace
    }

    @Test
    fun cipher_encrypts_payloads_at_rest() {
        val backing = MemoryStorage()
        val shelf = fastShelf(storage = backing, cipher = EncPrefixCipher())

        shelf.item("secret").put(7)

        val raw = backing.get("secret") // no namespace prefix on this overload
        assertTrue(raw != null && raw.startsWith("enc:"), "payload should be encrypted at rest")
        assertEquals(7, shelf.item("secret").get<Int>()) // still round-trips
    }
}
