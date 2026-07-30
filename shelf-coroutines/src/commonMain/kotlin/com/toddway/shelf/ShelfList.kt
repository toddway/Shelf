package com.toddway.shelf

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

/**
 * A reactive, persisted list backed by a single [Shelf.Item].
 *
 * The current list is exposed as a [StateFlow] so UI/observers can react to changes, and every
 * mutation is persisted through [shelf]. All storage access runs on [dispatcher] via
 * [withContext], so callers on the main thread are never blocked on disk I/O. On the JVM/Android,
 * pass `Dispatchers.IO`; the default [Dispatchers.Default] is the multiplatform-safe fallback.
 *
 * Call [load] once to populate the flow from storage before observing.
 */
open class ShelfList<T : Any>(
    val shelf: Shelf,
    val key: String,
    val type: KClass<T>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val shelfItem = shelf.item(key)
    private val _flow = MutableStateFlow<List<T>>(emptyList())
    val flow: StateFlow<List<T>> = _flow.asStateFlow()

    /** Loads the persisted list into [flow], or an empty list if nothing is stored. */
    suspend fun load() {
        _flow.value = withContext(dispatcher) { shelfItem.getList(type) } ?: emptyList()
    }

    suspend fun add(item: T) = set(_flow.value + item)

    suspend fun remove(item: T) = set(_flow.value - item)

    /** Replaces the list, persisting it and then publishing the new value to [flow]. */
    suspend fun set(list: List<T>) {
        withContext(dispatcher) { shelfItem.put(list) }
        _flow.value = list
    }
}
