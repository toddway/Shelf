package com.toddway.shelf

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Composes a high-performance [Shelf]: [storage] is wrapped in an in-memory LRU cache
 * ([CachingStorage]) behind a single lock ([ThreadSafeStorage]), and (de)serialization runs through
 * a cached [KotlinxSerializer]. Pass a [cipher] to encrypt payloads at rest, or a configured [json]
 * to control serialization.
 *
 * This is an opinionated preset over the raw `Shelf(storage, serializer)` constructor — reach for it
 * when you want caching and thread-safety without composing the decorators yourself.
 */
@OptIn(InternalSerializationApi::class)
fun fastShelf(
    storage: Shelf.Storage,
    cipher: Cipher? = null,
    json: Json = Json.Default,
): Shelf = Shelf(
    storage = ThreadSafeStorage(CachingStorage(storage)),
    serializer = CachingSerializer(
        if (cipher != null) SecureSerializer(cipher, KotlinxSerializer(json))
        else KotlinxSerializer(json),
    ),
)

/**
 * Like [fastShelf], but isolates this shelf under [name] (via [NamespacedStorage]) so multiple named
 * shelves can share one physical [storage] without colliding — the portable way to run more than one
 * shelf in a single multiplatform app.
 *
 * [storage] defaults to the platform's [DiskStorage] (files on JVM, `NSUserDefaults` on iOS/Native,
 * `localStorage` on JS). On the JVM, prefer `File.fastShelf`, which isolates each shelf in its own
 * directory instead of sharing one by key prefix.
 */
@OptIn(InternalSerializationApi::class)
fun fastShelf(
    name: String,
    storage: Shelf.Storage = DiskStorage(),
    cipher: Cipher? = null,
    json: Json = Json.Default,
): Shelf = fastShelf(NamespacedStorage(storage, name), cipher, json)
