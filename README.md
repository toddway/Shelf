# Shelf
Local object storage for Java and Android.  Includes...

- Simple & fluent API for key-value storage
- Convenient timestamp evaluation
- Canned cache policies with RxJava Observables
- Pluggable storage interface (Default is flat file storage. Roll your own- DiskLRUCache, Shared Preferences, SQLite,  etc.)
- Pluggable serialization interface (Default is Gson.  Roll your own - Jackson, Kryo, etc.)
 


## How to use
Install from build.gradle:

```groovy
    repositories {
        maven {
            url  "http://dl.bintray.com/toddway/maven" 
        }
    }
    
    dependencies {
        compile 'com.toddway:shelf:1.1.0'
    }
```    


Initialize:

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
shelf.defaultLifetime(1, MILLISECOND);
shelf.item("myString").put("cached value");
Observable<String> myObservable = Observable.fromCallable(() -> return "new value");


//Prints "cached value" then prints "new value".
myObservable
    .compose(shelf.item("myString").cacheThenNew(String.class))
    .subscribe(s -> System.out.println(s));
     

//Prints "new value" if the cache is older than 1 millisecond, otherwise it prints "cached value".
myObservable
    .compose(shelf.item("myString").cacheOrNew(String.class))
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