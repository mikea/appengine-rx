package com.mikea.gae.rx.base;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Injector;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author mike.aizatsky@gmail.com
 */
public final class Observables {

    private Observables() { }

    public static <S, T> IObservable<T> transform(IObservable<S> src, Class<? extends Function<S, T>> functionClass, Injector injector) {
        return transform(src, injector.getInstance(functionClass));
    }

    public static <T, S> IObservable<T> transform(final IObservable<S> src, final Function<S, T> f) {
        return transform(src, new DoFn<S, T>() {
            @Override
            public void process(S s, EmitFn<T> emitFn) throws IOException {
                emitFn.emit(f.apply(s));
            }
        });
    }

    public static <T> void apply(IObservable<T> src, Class<? extends IAction<T>> actionClass, Injector injector) {
        apply(src, injector.getInstance(actionClass));
    }

    public static <T> IObservable<T> apply(IObservable<T> src, IAction<T> action) {
        src.subscribe(Observers.asObserver(action));
        return src;
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
                    public void onNext(T value) throws IOException {
                        if (predicate.apply(value)) {
                            observer.onNext(value);
                        }
                    }
                });
            }
        };
    }

    public static <T> IObservable<T> flatten(final IObservable<Iterable<T>> src) {
        return new IObservable<T>() {
            @Override
            public IDisposable subscribe(final IObserver<T> observer) {
                return src.subscribe(new IObserver<Iterable<T>>() {
                    @Override
                    public void onCompleted() {
                        observer.onCompleted();
                    }

                    @Override
                    public void onError(Exception e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onNext(Iterable<T> values) throws IOException {
                        for (T t : values) {
                            observer.onNext(t);
                        }
                    }
                });
            }
        };
    }

    public static <T> IObservable<T> sink(IObservable<T> src, IObserver<T> sink) {
        src.subscribe(sink);
        return src;
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
                    public void onNext(T value) throws IOException {
                        for (U u : checkNotNull(fn.apply(value))) {
                            observer.onNext(u);
                        }
                    }
                });
            }
        };
    }

    public static <T, U> IObservable<U> transform(final IObservable<T> src, final DoFn<T, U> fn) {
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
                    public void onNext(T value) throws IOException {
                        fn.process(value, new EmitFn<U>() {
                            @Override
                            public void emit(U u) throws IOException {
                                observer.onNext(u);
                            }
                        });
                    }
                });
            }
        };
    }
}
