# Shelf
Local object storage for Java and Android.  Includes...

- Simple & fluent API for key-value storage
- Convenient timestamp evaluation
- Canned cache policies with RxJava Observables
- Pluggable storage interface (Default is flat file storage. Roll your own- DiskLRUCache, Shared Preferences, SQLite,  etc.)
- Pluggable serialization interface (Default is Gson.  Roll your own - Jackson, Kryo, etc.)
 


## How to use
Initialize:

```java
//with defaults
Shelf shelf = new Shelf(new File("/tmp/shelf"));

//with customizations
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



Canned cache policies with RxJava
```java
shelf.item(key).put("cached value");
shelf.item(key)
        .policies(String.class, Observable.fromCallable(() -> return "new value"))
        .observeCacheThenNew()
        .subscribe((s) -> System.out.println(s)) //prints "cached value" then "new value" 
```