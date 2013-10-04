package com.mikea.gae.rx;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Injector;

/**
 * @author mike.aizatsky@gmail.com
 */
class Observables {

    public static <S, T> IObservable<T> transform(IObservable<S> src, Class<? extends Function<S, T>> functionClass, Injector injector) {
        return transform(src, injector.getInstance(functionClass));
    }

    public static <T, S> IObservable<T> transform(final IObservable<S> src, final Function<S, T> f) {
        return new IObservable<T>() {
            @Override
            public IDisposable subscribe(final IObserver<T> observer) {
                return src.subscribe(new IObserver<S>() {
                    @Override
                    public void onCompleted() {
                        observer.onCompleted();
                    }

                    @Override
                    public void onError(Exception e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onNext(S value) {
                        observer.onNext(f.apply(value));
                    }
                });
            }
        };
    }

    public static <T> void apply(IObservable<T> src, Class<? extends IAction<T>> actionClass, Injector injector) {
        apply(src, injector.getInstance(actionClass));
    }

    private static <T> void apply(IObservable<T> src, IAction<T> action) {
        src.subscribe(Observers.asObserver(action));
    }

    public static <T> IObservable<T> filter(final IObservable<T> src, final Predicate<T> predicate) {
        return new IObservable<T>() {
            @Override
            public IDisposable subscribe(final IObserver<T> observer) {
                return src.subscribe(new IObserver<T>() {
                    @Override
                    public void onCompleted() {
                        observer.onCompleted();
                    }

                    @Override
                    public void onError(Exception e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onNext(T value) {
                        if (predicate.apply(value)) {
                            observer.onNext(value);
                        }
                    }
                });
            }
        };
    }

    public static <T> void sink(IObservable<T> src, IObserver<T> sink) {
        src.subscribe(sink);
    }

    public static <T, U> IObservable<U> transformMany(IObservable<T> src, Class<? extends Function<T, Iterable<U>>> fnClass, Injector injector) {
        return transformMany(src, injector.getInstance(fnClass));
    }

    public static <T, U> IObservable<U> transformMany(final IObservable<T> src, final Function<T, Iterable<U>> fn) {
        return new IObservable<U>() {
            @Override
            public IDisposable subscribe(final IObserver<U> observer) {
                return src.subscribe(new IObserver<T>() {
                    @Override
                    public void onCompleted() {
                        observer.onCompleted();
                    }

                    @Override
                    public void onError(Exception e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onNext(T value) {
                        for (U u : fn.apply(value)) {
                            observer.onNext(u);
                        }
                    }
                });
            }
        };
    }
}
