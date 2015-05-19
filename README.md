# Shelf
Local object storage for Android.  Includes...

- Simple, fluent API, for key-value access
- Convenient timestamp evaluation
- Gson encoding (support for storing complex custom objects, Generics)
- Pluggable storage options (Default is flat files. Roll your own - DiskLRUCache, Shared Preferences, SQLite, etc.)


## How to use
Initialize:

    Shelf.init(context); //defaults to internal file storage

    ...or...

    Shelf.init(new LRUStorage(context, versionId, 1000000)); //init with custom storage options

Put an object:

    Shelf.item("myObject").put(new MyObject());

Get an object:

    MyObject myObject = Shelf.item("myObject").get(MyObject.class);

Use TypeToken to get complex object types:

    List<MyObject> list = Shelf.item("myObjectList").get(new TypeToken<List<MyObject>>(){});


Check the age:

    Shelf.item("myObject").isOlderThan(10, TimeUnit.MINUTES) //true if item is older than 10 min or does not exist, false otherwise

Bulk delete items by prefix:

    Shelf.clear("object_");


Write time-based caching policies:

    ShelfItem<MyObject> item = Shelf.item("myObject");
    if (item.isOlderThan(10, TimeUnit.MINUTES)) {
        myObject = fetchRemoteObject(...);
        item.put(myObject);
    }
    MyObject myObject = item.get(MyObject.class);


Combine with RxJava for async policies:

    Observable<MyObject> ob = Observable.create((subscriber) -> {
        ShelfItem<MyObject> item = Shelf.item("myObject");
        if (item.exists()) {
            subscriber.onNext(item.get(MyObject.class);
        }
        MyObject myObject = fetchNetworkObject(...);
        subscriber.onNext(myObject);
        subscriber.onCompleted();
        item.put(myObject);
    });


Clear items on version changes:

    int version = BuildConfig.VERSION_CODE;

    Shelf.init(context);
    ...
    if (Shelf.item("version").get() != version)
        Shelf.clear("");
        Shelf.item("version").put(version);
    }


Implement your own storage options:

    public interface Storage {

        public String get(String key) throws IOException;

        public boolean put(String key, String value) throws IOException;

        public boolean delete(String key) throws IOException;

        public boolean contains(String key);

        public long lastModified(String key);

        public List<String> keys(String startsWith);
    }

