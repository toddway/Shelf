# Shelf
Key/value object store for Kotlin. Persist any serializable object.  Multiplatform compatible - JVM, Android, JS, Native, iOS.

[![Release](https://img.shields.io/github/v/release/toddway/Shelf)](https://github.com/toddway/Shelf/releases)

## Basic usage

Initialize a shelf
```kotlin
val shelf = Shelf(FileStorage(applicationContext.cacheDir), MoshiSerializer())
```

Store an object instance
```kotlin
data class Thing(val id : Int, val name : String) //my custom type

shelf.item("thing").put(Thing(1, "thing 1"))
```

Get a previously stored item
```kotlin
val thing = shelf.item("thing").get<Thing>()

print(thing.name) //prints "thing 1"
```

Store and get typed lists
```kotlin
shelf.item("things").put(listOf(Thing(...), Thing(...))
val things = shelf.item("things").getList<Thing>()
```

Get a previously stored item from disk unless it is older than 60 seconds
```kotlin
fun newListOfThings() : List<Thing> = ...

fun getListOfThings() =
    shelf.item("things")
        .apply { if (olderThan(60)) put(newListOfThings()) }
        .getList<Thing>()
```

Remove an item
```kotlin
shelf.item("thing").remove()
```

Remove all items
```kotlin
shelf.clear()
```

Remove only items older than 60 seconds
```kotlin
shelf.all().filter { it.olderThan(60) }.forEach { it.remove() }
```


## Serialization
The default serializer for Shelf depends on the [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) library.
Primitive type classes work automatically.
For custom classes, annotate with `@Serializable`, and register with Shelf:
```kotlin
@Serializable
data class Thing(...)

@Serializable
data class Whatever(...)

shelf.serializer = KotlinxSerializer().apply {
    register(Thing.serializer())
    register(Whatever.serializer())
}
```

For JVM targets, there is also a `MoshiSerializer` for [Moshi](https://github.com/square/moshi) and a `GsonSerializer` for [Gson](https://github.com/google/gson).

## Storage
The `DiskStorage` class depends on delegates for each platform.
By inspecting the code, you can see that
for Kotlin/Native targets, the delegate is `NSUserDefaults`,
for Kotlin/JS targets, it is `LocalStorage`,
and for Kotlin/JVM it is `File("/tmp")`.
Native and JS platforms should work without configuration.
For JVM environments, you should choose an appropriate location on the file system to use.
For example, on Android, this can be acquired from `Context.getCacheDir()`.

```kotlin
val shelf = Shelf(FileStorage(context.getCacheDir()))
```

You can also create your own own implementations of `Shelf.Storage` or `Shelf.Serializer`.
```kotlin
val shelf = Shelf(MyOwnStorage(...), MyOwnSerializer(...))
```

## Gradle

Shelf is published to a GitHub Pages Maven repo, so add that repository:

```groovy
repositories {
    maven { url = uri("https://toddway.github.io/Shelf/") }
}
```

```groovy
dependencies {
    implementation 'com.toddway.shelf:shelf:3.0.0'
    implementation 'com.toddway.shelf:shelf-coroutines:3.0.0' // optional: ShelfList (coroutines/Flow)
}
```

## For library developers

### Build checks
The following command runs the tests and code analyzers and prints a gated summary with a link to
the HTML report ([BuildChecks](https://github.com/toddway/BuildChecks)):
```
./gradlew build :koverXmlReport detekt buildchecks
```
CI runs the same gate on every push and pull request (see [.github/workflows/ci.yml](.github/workflows/ci.yml)).

### Publishing
Releases publish every Kotlin Multiplatform artifact to a [GitHub Pages Maven repo](https://toddway.github.io/Shelf)
using only the built-in `GITHUB_TOKEN` — no signing keys or Sonatype account. Cut a release with a
single command:
```
./release.sh 3.0.1 --push
```
This bumps `VERSION_NAME`, verifies the build, commits, and tags `v3.0.1`; the tag triggers
[.github/workflows/release.yml](.github/workflows/release.yml), which publishes the artifacts to the
`gh-pages` branch and creates a GitHub Release. Omit `--push` to prepare the commit/tag and inspect it
before pushing.


License
-------

    Copyright 2016-Present Todd Way

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.