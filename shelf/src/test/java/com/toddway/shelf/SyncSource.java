package com.toddway.shelf;

import io.reactivex.Observable;

/**
 * Created by tway on 1/11/17.
 */

public class SyncSource<T>  {
    private long timestamp = -1;
    Observable<T> observeServerRequest;


    public void loadIfNeeded() {
//        super.loadIfNeeded();
//        Observable.just(timestamp > (System.currentTimeMillis() - 60000))
//                .filter(new Func1<Boolean, Boolean>() {
//                    @Override
//                    public Boolean call(Boolean aBoolean) {
//                        return aBoolean;
//                    }
//                })
//                .flatMap(new Func1<Boolean, Observable<T>>() {
//                    @Override
//                    public Observable<T> call(Boolean aBoolean) {
//                        return observeServerRequest;
//                    }
//                })
//                .flatMap(new Func1<T, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(T t) {
//                        return update(t);
//                    }
//                })
////                .doOnNext(new Consumer<T>() {
////                    @Override
////                    public void call(T t) {
////                        timestamp = System.currentTimeMillis();
////                    }
////                })
//                .subscribe();

//        if (timestamp > (System.currentTimeMillis() - 60000)) { //older than 1 minute
//            observeServerRequest
//                    .flatMap(new Func1<T, Observable<?>>() {
//                        @Override
//                        public Observable<?> call(T t) {
//                            return updateCache(t);
//                        }
//                    })
//                    .subscribe();
    }
}
