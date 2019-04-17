# Shelf
Kotlin key/value store for Kotlin that can persist any [serializable](https://github.com/Kotlin/kotlinx.serialization) object on multiple kotlin platforms - JVM, JS, Native.  
 
## Basic usage

Store an object
```kotlin
Shelf.item("my object").put(MyObj.serializer(), myObj)
```
Get it
```kotlin
val myObj = Shelf.item("my object").get(MyObj.serilaizer())
```

Get, if max age in milliseconds has not expired 
```kotlin
val myObj = Shelf.item("my object").get(MyObj.serilaizer(), maxAge = 1000)
```

Remove it
```kotlin
Shelf.item("my object").remove()
```

Remove all
```kotlin
Shelf.all().forEach { it.remove() }
```

Remove only items older than 60 seconds
```kotlin

Shelf.all().filter { it.age() > 60000 }.forEach { it.remove() }
```


## Gradle setup
For use on a single JVM platform (including Android): 
```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'com.toddway.shelf:shelf-jvm:$shelf_version'
}
```    

For multiplatform use:

Common source set
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

## Storage configuration
For each platform, an implementation of the expected `DiskStorage` class is automatically initialized for shelf storage:

```kotlin
Shelf.storage = DiskStorage()
```  

By reviewing the `DiskStorage` code for each platform, 
you will see that for Kotlin/Native targets (including iOS), it uses `NSUserDefaults`,
for Kotlin/JS targets, it uses `LocalStorage`,
and for Kotlin/JVM it uses the /tmp directory of the file system - `File("/tmp")`. 
Native and JS platforms should work with no custom initialization. 
For JVM environments, you will likely want to configure an appropriate directory. 
For example, on Android, this could be acquired from `Context.getCacheDir()`. 

```kotlin
Shelf.storage = DiskStorage(context.cacheDir)   
```

You can also implement your own `Shelf.Storage`:

```kotlin
Shelf.storage = object : Shelf.Storage {
    override fun get(key: String): String? {}
    override fun put(key: String, item: String) {}
    override fun age(key: String): Long {}
    override fun keys(): Set<String> {}
    override fun remove(key: String) {}
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
./gradle karma-run-single
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