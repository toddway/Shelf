# Shelf
Key/value object store for Kotlin. Persist any serializable object.  Multiplatform compatible - JVM, Android, JS, Native, iOS.

[ ![Download](https://api.bintray.com/packages/toddway/maven/shelf/images/download.svg) ](https://bintray.com/toddway/maven/shelf/_latestVersion)

## Basic usage

Store an object
```kotlin
val something = Something(...)
val item = Shelf.item("something").put(something)
```

Get it
```kotlin
Shelf.item("something").get<Something>()
```

Get a list of objects
```kotlin
Shelf.item("something list").getList<Something>()
```

Remove it
```kotlin
Shelf.item("something").remove()
```

Remove all
```kotlin
Shelf.all().forEach { it.remove() }
```

Remove only items older than 60 seconds
```kotlin

Shelf.all().filter { it.olderThan(60) }.forEach { it.remove() }
```

If item is older than 60 seconds, fetch new data, then use shelf
```kotlin
Shelf.item(key)
    .apply { if (olderThan(60)) put(newListOfThings()) }
    .getList<Thing>()
```


## Serialization
The default serializer for Shelf depends on the [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) library.
Primitive type classes work automatically.
For custom classes, annotate with `@Serializable`, and register with Shelf:
```kotlin
@Serializable
data class Something(...)

@Serializable
data class Whatever(...)

Shelf.serializer = KotlinxSerializer().apply {
    register(Something.serializer())
    register(Whatever.serializer())
}
```

For JVM targets, there is also a `MoshiSerializer` for [Moshi](https://github.com/square/moshi).

## Storage
The `DiskStorage` class depends on delegates for each platform.
By inspecting the code, you can see that
for Kotlin/Native targets, the delegate is `NSUserDefaults`,
for Kotlin/JS targets, it is `LocalStorage`,
and for Kotlin/JVM it is `File`.
Native and JS platforms should work without configuration.
For JVM environments, you should set the location on the file system that `DiskStorage` will use.
For example, on Android, this can be acquired from `Context.getCacheDir()`.

```kotlin
Shelf.storage = DiskStorage(context.getCacheDir())
```



## Gradle setup

```groovy
repositories {
    jcenter()
}
```    

Common multiplatform source set
```groovy
dependencies {
    implementation 'com.toddway.shelf:shelf:$shelf_version'
}
```

iOS/Native source set
```groovy
dependencies {
    implementation 'com.toddway.shelf:shelf-ios:$shelf_version'
}
```

Android/JVM source set
```groovy
dependencies {
    implementation 'com.toddway.shelf:shelf-jvm:$shelf_version'
}
```

Javascript source set
```groovy
dependencies {
    implementation 'com.toddway.shelf:shelf-js:$shelf_version'
}
```



## Running tests
The library has common tests that can be run (and should pass) on a local JVM:

```
./gradlew jvmTest
```
 
a local iOS simulator:
```
./gradlew iosTest
```
  
and a local web browser:
```
./gradlew karma-run-single
```
or all three:
```
./gradlew printChecks
```

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