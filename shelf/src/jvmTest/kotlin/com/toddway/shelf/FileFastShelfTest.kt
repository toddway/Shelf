package com.toddway.shelf

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Covers the JVM-only `File.fastShelf` extension: subdirectory isolation over real files. */
class FileFastShelfTest {

    private val root: File = Files.createTempDirectory("shelf-test").toFile()

    @Test
    fun value_round_trips_through_a_named_subdirectory() {
        val shelf = root.fastShelf("user")
        shelf.item("count").put(42)

        assertEquals(42, shelf.item("count").get<Int>())
        assertTrue(File(root, "user").isDirectory, "named shelf should get its own subdirectory")
    }

    @Test
    fun named_shelves_are_isolated_by_directory() {
        val user = root.fastShelf("user")
        val app = root.fastShelf("app")

        user.item("count").put(1)
        app.item("count").put(2)

        user.clear()
        assertNull(user.item("count").get<Int>())
        assertEquals(2, app.item("count").get<Int>()) // separate directory, untouched
    }
}
