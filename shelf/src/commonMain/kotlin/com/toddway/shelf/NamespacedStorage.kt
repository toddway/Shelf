package com.toddway.shelf

/**
 * Isolates keys under a [name] so several logical shelves can share one physical [Shelf.Storage]
 * without colliding. Every key is stored as `"<name>.<key>"`, and [keys] reports only the keys that
 * belong to this namespace (with the prefix stripped) — so [Shelf.clear] on one named shelf never
 * touches another.
 *
 * This is the portable way to run multiple named shelves over a single [DiskStorage] on any target
 * (`NSUserDefaults` on iOS/Native, `localStorage` on JS, a shared directory on JVM). On the JVM you
 * can alternatively give each shelf its own directory with `FileStorage(File(root, name))` — see
 * `File.fastShelf`.
 *
 * [name] must be non-blank and must not contain the `.` separator (names that did could overlap:
 * namespace `a` with key `b.c` and namespace `a.b` with key `c` would share a physical key). On the
 * JVM, [FileStorage] sanitizes stored keys to filename-safe characters, so a [name] should contain
 * only characters that survive that (`[a-zA-Z0-9-]`) to keep [keys] matching.
 */
class NamespacedStorage(
    private val delegate: Shelf.Storage,
    name: String,
) : Shelf.Storage {

    init {
        require(name.isNotBlank()) { "Namespace name must not be blank" }
        require(!name.contains(SEPARATOR)) { "Namespace name must not contain '$SEPARATOR': $name" }
    }

    private val prefix = "$name$SEPARATOR"

    override fun get(key: String): String? = delegate.get(prefix + key)

    override fun put(key: String, value: String, timestamp: Long) =
        delegate.put(prefix + key, value, timestamp)

    override fun timestamp(key: String): Long? = delegate.timestamp(prefix + key)

    override fun remove(key: String) = delegate.remove(prefix + key)

    override fun keys(): Set<String> =
        delegate.keys().filter { it.startsWith(prefix) }.map { it.removePrefix(prefix) }.toSet()

    private companion object {
        const val SEPARATOR = "."
    }
}
