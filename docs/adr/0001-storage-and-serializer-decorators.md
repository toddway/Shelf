# Storage and Serializer Decorators (Caching, Thread-Safety, Encryption, Reactive Lists)

## Status

Accepted — 2026-07-30

## Context

Shelf ships a deliberately small core: two extension points, `Shelf.Storage` and
`Shelf.Serializer`, plus a `DiskStorage`/`FileStorage` and a handful of serializers
(`KotlinxSerializer`, `GsonSerializer`, `MoshiSerializer`). Everything else — caching,
thread-safety, encryption, reactive access — is left to the consumer.

In practice, every non-trivial consumer re-implements the same cross-cutting concerns on
top of those two interfaces. The Sherwin-Williams PRO Android app (which is migrating its
local storage off Realm onto Shelf, see that project's ADR 0006) independently wrote a full
decorator stack around Shelf:

```
Storage:    ThreadSafeStorage( CachingStorage( FileStorage ) )
Serializer: CachingSerializer( SecureSerializer( KotlinxSerializer | GsonSerializer ) )
```

plus an in-memory `MemoryStorage` for tests and a Flow-backed `ShelfList`. This is
exactly the seam Shelf was designed for, and the pieces are generic enough that keeping them
in the app means every other Shelf consumer reinvents them. Two concrete problems motivated
pulling them into the library:

1. **Missing building blocks.** Shelf has no in-memory storage (only `DiskStorage`), no
   thread-safety guarantee (`FileStorage` is unsafe under concurrent access to the same key),
   no read-through cache, no encrypt-at-rest option, and no reactive/Flow API at all.

2. **Correctness issues discovered in the app's implementation** that should not be copied
   into other apps:
   - `CachingSerializer` keyed its object cache on `string.hashCode()`, so two distinct JSON
     payloads with a hash collision could return the wrong cached object.
   - `CachingSerializer` returns shared references to cached deserialized objects; a caller
     that mutates a result silently corrupts the cache. (Shelf's own test suite mutates a
     retrieved object, so this contract must be explicit.)
   - The app re-implemented a private `GsonSerializer` identical to the one Shelf already
     ships — pure duplication.

The constraint is that Shelf is a Kotlin Multiplatform library. The app's decorators lean on
JVM-only APIs — `java.util.concurrent.locks.ReentrantReadWriteLock`, the access-order
`LinkedHashMap(capacity, 0.75f, true)` constructor, and `kotlin.synchronized` — none of which
exist in `commonMain`. Naively lifting the code would strand it in `jvmMain`.

## Decision

Promote the generic decorators into Shelf as first-class, **multiplatform** components, fixing
the correctness issues on the way in, and add a reactive layer as a separate module so the
dependency-free core stays dependency-free.

### 1. Multiplatform concurrency primitive

Add `kotlinx-atomicfu` to the core `shelf` module's `commonMain` and use
`kotlinx.atomicfu.locks` for all shared-state synchronization. This replaces the JVM-only
`ReentrantReadWriteLock`/`synchronized` with a single multiplatform reentrant lock.

We accept a **single reentrant lock** (no reader/writer distinction) rather than an
`expect`/`actual` read-write lock. It is far simpler and truly multiplatform; the read-write
optimization is unnecessary because `CachingStorage` sits in front of disk and absorbs the
overwhelming majority of reads from memory. A JVM-specialized RW lock can be added later via
`expect`/`actual` without changing the public API if profiling ever justifies it.

### 2. Storage decorators → `commonMain`

- **`MemoryStorage`** — in-memory `Shelf.Storage`, internally synchronized (a bare map throws
  `ConcurrentModificationException` under concurrent iteration). Doubles as the base storage
  for the library's own tests, making them fast and hermetic.
- **`ThreadSafeStorage`** — locking decorator that makes any `Storage` (notably the unsafe
  `FileStorage`) safe for concurrent use, using the atomicfu reentrant lock.
- **`CachingStorage`** — read-through LRU cache decorator. Replaces the JVM access-order
  `LinkedHashMap` constructor with a small multiplatform LRU (insertion-order `LinkedHashMap`
  with remove-then-reinsert on access) guarded by the lock.

### 3. Serializer decorators → `commonMain`

- **`CachingSerializer`** — caches deserialized objects, with both correctness fixes applied:
  the cache is keyed on the **full payload string** (plus type), eliminating the hash-collision
  risk and giving free invalidation when a value changes; and its KDoc states the
  **immutable-types-only** contract explicitly. It remains opt-in and is not wired into any
  default `Shelf`.
- **`SecureSerializer` + `Cipher`** — encrypt-at-rest decorator, decoupled from any app's
  crypto by introducing a minimal library-owned interface:
  ```kotlin
  interface Cipher {
      fun encrypt(plaintext: String): String
      fun decrypt(ciphertext: String): String
  }
  ```
  Consumers supply a one-line adapter from their own crypto to `Cipher`.

### 4. Reactive layer → new `shelf-coroutines` module

`ShelfList` (a `StateFlow`-backed list) needs `kotlinx-coroutines-core`. To keep the
core artifact dependency-free, it ships in a **separate `shelf-coroutines` module** rather than
in core. The repository takes a `CoroutineDispatcher` so disk work runs off the caller's thread
(the original app version called blocking disk ops inline from `suspend` functions).

### 5. App-side follow-up (out of scope for this repo, recorded for context)

Once published, the app deletes its copies of all six decorators **and its duplicate private
`GsonSerializer`**, then rewires to the library types, keeping only its app-specific glue
(the `File.shelf()` / `secureGsonShelf()` factories and a `Crypto → Cipher` adapter).

## Consequences

- **Positive:** Every Shelf consumer gets production-grade caching, thread-safety, encryption,
  an in-memory storage, and a reactive list API out of the box. The two correctness bugs are
  fixed once, in the library, instead of being copied across apps. Core stays dependency-free
  apart from a small atomicfu addition.
- **Negative / trade-offs:**
  - The core module gains a dependency on `kotlinx-atomicfu`.
  - `ThreadSafeStorage` serializes all storage operations (single lock); acceptable given the
    fronting cache, revisitable via `expect`/`actual` later.
  - `CachingSerializer` is safe only for immutable types — a documented contract, not an
    enforced one.
  - A new published artifact (`shelf-coroutines`) adds release/maintenance surface.
- **Compatibility:** All additions are new types; nothing in the existing public API changes,
  so this is a minor, backward-compatible release (target `2.1.0`).
