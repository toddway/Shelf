package com.toddway.shelf

import java.util.concurrent.locks.ReentrantLock

internal actual class Lock {
    private val delegate = ReentrantLock()
    actual fun acquire(): Unit = delegate.lock()
    actual fun release(): Unit = delegate.unlock()
}
