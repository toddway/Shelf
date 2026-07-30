package com.toddway.shelf

// JavaScript is single-threaded, so mutual exclusion is a no-op.
internal actual class Lock {
    actual fun acquire() {}
    actual fun release() {}
}
