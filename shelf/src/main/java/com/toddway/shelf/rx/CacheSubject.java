package com.toddway.shelf.rx;

import org.reactivestreams.Subscriber;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subscribers.DisposableSubscriber;
import io.reactivex.subscribers.ResourceSubscriber;

public class CacheSubject<T> extends Subject<T> {

    private Source<T> cacheSource;
    private BehaviorSubject<T> behaviorSubject;

    protected CacheSubject(ObservableEmitter<T> onSubscribe, Source<T> cacheSource, BehaviorSubject<T> behaviorSubject) {
        //super(onSubscribe);
        super();
        this.cacheSource = cacheSource;
        this.behaviorSubject = behaviorSubject;
    }

    interface Source<T> {
        Observable<T> read();
        Observable<T> write(T t);
    }

    public static <T> CacheSubject<T> create(final Source<T> source) {
        final BehaviorSubject<T> subject = BehaviorSubject.create();

        Observer<T> onSubscribe = new Observer<T>() {

            public void call(Observer<? super T> subscriber) {
                subject.subscribe(subscriber);
                if (!subject.hasValue()) {
                    source.read().subscribe(emitOnNext(subject));
                }
            }

            @Override
            public void onSubscribe(@NonNull Disposable d) { }

            @Override
            public void onError(@NonNull Throwable e) { }

            @Override
            public void onComplete() { }

            @Override
            public void onNext(@NonNull T t) { }
        };

        return new CacheSubject<>((ObservableEmitter) onSubscribe, (Source)source, (BehaviorSubject)subject);
    }

    @Override
    public boolean hasObservers() {
        return behaviorSubject.hasObservers();
    }

    @Override
    public void onComplete() {
        behaviorSubject.onComplete();
    }

    @Override
    public boolean hasComplete() {
        return false;
    }

    @Override
    public void onError(Throwable e) {
        behaviorSubject.onError(e);
    }

    @Override
    public void onNext(T t) {
        updateCache(t).subscribe();
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) { }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    @Override
    public boolean hasThrowable() {
        return false;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) { }

    public T getValue() {
        return behaviorSubject.getValue();
    }

    public Observable<T> updateCache(T t) {
        return cacheSource.write(t).doOnNext(emitOnNext(behaviorSubject));
    }

    private static <T> Consumer<T> emitOnNext(final BehaviorSubject<T> behaviorSubject) {
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                behaviorSubject.onNext(t);
            }
        };
    }
}
