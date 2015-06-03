# Shelf
Local object storage for Java and Android.  Includes...

- Simple, fluent API, for key-value access
- Convenient timestamp evaluation
- Optional Gson serialization
- Pluggable storage options (Default is java serialized files. Roll your own - DiskLRUCache, Shared Preferences, SQLite, etc.)


## How to use
Initialize:

    //with no external dependencies
    Shelf myShelf = new Shelf(new File("/tmp/myshelf"));


    //with optional Gson module dependency
    Shelf myShelf = new Shelf(new GsonFileStorage(new File("/tmp/myshelf")));


    //with Gson and Android dependencies
    Shelf myShelf = new Shelf(new GsonFileStorage(getContext().getDir("myshelf", Context.MODE_PRIVATE)));


Store any object:

    myShelf.item("myObject").put(new Pojo());

Get any object:

    Pojo myPojo = myShelf.item("myPojo").get(Pojo.class);

Get any list:

    List<Pojo> list = myShelf.item(key).getListOf(Pojo.class);


Check the age of an item:

    myShelf.item("myPojo").isOlderThan(10, TimeUnit.MINUTES) //true if item is older than 10 min or does not exist, false otherwise

Bulk delete items by prefix:

    myShelf.clear("pojo_");


Write time-based caching policies:

    ShelfItem<MyObject> item = myShelf.item("myObject");
    if (item.isOlderThan(10, TimeUnit.MINUTES)) {
        myObject = fetchRemoteObject(...);
        item.put(myObject);
    }
    MyObject myObject = item.get(MyObject.class);


Combine with RxJava for async policies:

    Observable<MyObject> ob = Observable.create((subscriber) -> {
        ShelfItem<MyObject> item = myShelf.item("myObject");
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
    ...
    if (myShelf.item("version").get() != version)
        myShelf.clear("");
        myShelf.item("version").put(version);
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

