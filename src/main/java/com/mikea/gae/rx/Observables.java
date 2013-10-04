package com.mikea.gae.rx;

import com.google.common.base.Function;
import com.google.inject.Injector;

/**
 * @author mike.aizatsky@gmail.com
 */
class Observables {

    public static <S, T> IObservable<T> transform(IObservable<S> src, Class<? extends Function<S, T>> functionClass, Injector injector) {
        return transform(src, injector.getInstance(functionClass));
    }

    private static <T, S> IObservable<T> transform(final IObservable<S> src, Function<S, T> f) {
        return new IObservable<T>() {
            @Override
            public IDisposable subscribe(IObserver<T> observer) {
                return src.subscribe(new IObserver<S>() {
                    @Override
                    public void onCompleted() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void onError(Exception e) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void onNext(S value) {
                        throw new UnsupportedOperationException();
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
}
