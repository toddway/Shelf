package com.toddway.shelf

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File

/**
 * JVM/Android convenience for [fastShelf] that stores each shelf in its own subdirectory [name]
 * under the receiver directory — e.g. `context.cacheDir.fastShelf("user")`. Because each named shelf
 * gets a real directory, [Shelf.clear] on one never touches another. Pass a [cipher] to encrypt
 * payloads at rest, or a configured [json] to control serialization.
 */
@OptIn(InternalSerializationApi::class)
fun File.fastShelf(
    name: String,
    cipher: Cipher? = null,
    json: Json = Json.Default,
): Shelf = fastShelf(FileStorage(File(this, name)), cipher, json)
