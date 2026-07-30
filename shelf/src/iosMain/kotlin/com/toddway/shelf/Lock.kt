package com.toddway.shelf

import platform.Foundation.NSRecursiveLock

internal actual class Lock {
    private val delegate = NSRecursiveLock()
    actual fun acquire(): Unit = delegate.lock()
    actual fun release(): Unit = delegate.unlock()
}
