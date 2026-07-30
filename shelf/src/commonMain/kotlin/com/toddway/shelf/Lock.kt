package com.toddway.shelf

/**
 * Minimal multiplatform reentrant mutual-exclusion lock.
 *
 * Backed by the most appropriate primitive on each target: `java.util.concurrent.locks.ReentrantLock`
 * on the JVM, `NSRecursiveLock` on Apple platforms, and a no-op on single-threaded JS.
 *
 * We deliberately do **not** use `kotlinx.atomicfu.locks.synchronized`: its value-returning `inline`
 * form only works where the atomicfu Gradle plugin transforms it (JVM), and that transform is broken
 * on the Kotlin/Native and JS IR backends — the block's return value comes back wrong (a `Long` reads
 * back as `0`, `toSet()` leaks the live backing set). A plain expect/actual lock plus the [withLock]
 * inline below sidesteps all of that and behaves identically on every platform.
 */
internal expect class Lock() {
    fun acquire()
    fun release()
}

/** Runs [block] while holding the lock, releasing it even if [block] throws. */
internal inline fun <T> Lock.withLock(block: () -> T): T {
    acquire()
    try {
        return block()
    } finally {
        release()
    }
}
