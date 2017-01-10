# Shelf
Local object storage for Java and Android.  Includes...

- Simple & fluent API for key-value storage
- Convenient timestamp evaluation
- Canned cache policies with RxJava Observables
- Pluggable storage interface (Default is flat file storage. Roll your own - DiskLRUCache, Shared Preferences, SQLite, etc.)
- Pluggable serialization interface (Default is Gson.  Roll your own - Jackson, Kryo, etc.)
 

## Install
Add jitpack to your root build.gradle:

```groovy
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add shelf dependency to the module build.gradle:

```groovy
    dependencies {
        compile 'com.toddway:shelf:X.X.X'
    }
```    

[![](https://jitpack.io/v/toddway/Shelf.svg)](https://jitpack.io/#toddway/Shelf)

## Usage

```java
//with defaults
Shelf shelf = new Shelf(new File("/tmp/shelf"));

//or with customizations
Shelf shelf = new Shelf(
                    new FileStorage(new File("/tmp"), 
                    new GsonSerializer(), 
                    TimeUnit.MINUTES.toMillis(1))
              );
```

Store any object:
```java
shelf.item("my pojo").put(new Pojo());
```
Get any object:
```java
Pojo pojo = shelf.item("my pojo").get(Pojo.class);
```

Get any list of objects:
```java
List<Pojo> list = Arrays.asList(shelf.item(key).get(Pojo[].class));
```

Check the age of an item:
```java
shelf.item(key).isOlderThan(10, TimeUnit.MINUTES) //true if item is older than 10 min or does not exist, false otherwise
```


Bulk delete items by prefix:
```java
shelf.clear("pojo_"); //deletes all items with keys starting pojo_
shelf.clear("") //deletes all items
```



Canned cache policies with RxJava:
```java
shelf.item("myString").put("cached value");
Observable<String> myObservable = Observable.fromCallable(() -> "new value");


//Prints "cached value" then prints "new value".
myObservable
    .compose(shelf.item("myString").maxAge(1, MINUTE).cacheThenNew(String.class))
    .subscribe(s -> System.out.println(s));
     

//Prints "new value" if the cache is older than 1 minute, otherwise it prints "cached value".
myObservable
    .compose(shelf.item("myString").maxAge(1, MINUTE).cacheOrNew(String.class))
    .subscribe(s -> System.out.println(s));     
```

Supported policies:
- cacheThenNew
- cacheOrNew
- newOnly
- pollNew
- cacheThenPollNew

"get" from Shelf as an Observable stream: 
```java

Observable<Pojo> whatever = shelf.item("whatever").getObservable(Pojo.class);
```

"put" to Shelf for Observable streams:
```java
myObservable.doOnNext(shelf.item("whatever").put());

```

License
-------

    Copyright 2016 Todd Way

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.