package com.toddway.shelf.rx;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class CacheSubject<T> extends Subject<T, T> {

    private Source<T> cacheSource;
    private BehaviorSubject<T> behaviorSubject;

    protected CacheSubject(OnSubscribe<T> onSubscribe, Source<T> cacheSource, BehaviorSubject<T> behaviorSubject) {
        super(onSubscribe);
        this.cacheSource = cacheSource;
        this.behaviorSubject = behaviorSubject;
    }

    interface Source<T> {
        Observable<T> read();
        Observable<T> write(T t);
    }

    public static <T> CacheSubject<T> create(final Source<T> source) {
        final BehaviorSubject<T> subject = BehaviorSubject.create();

        OnSubscribe<T> onSubscribe = new OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                subject.subscribe(subscriber);
                if (!subject.hasValue()) {
                    source.read().subscribe(emitOnNext(subject));
                }
            }
        };

        return new CacheSubject<>(onSubscribe, source, subject);
    }

    @Override
    public boolean hasObservers() {
        return behaviorSubject.hasObservers();
    }

    @Override
    public void onCompleted() {
        behaviorSubject.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        behaviorSubject.onError(e);
    }

    @Override
    public void onNext(T t) {
        updateCache(t).subscribe();
    }

    public T getValue() {
        return behaviorSubject.getValue();
    }

    public Observable<T> updateCache(T t) {
        return cacheSource.write(t).doOnNext(emitOnNext(behaviorSubject));
    }

    private static <T> Action1<T> emitOnNext(final BehaviorSubject<T> behaviorSubject) {
        return new Action1<T>() {
            @Override
            public void call(T t) {
                behaviorSubject.onNext(t);
            }
        };
    }
}
