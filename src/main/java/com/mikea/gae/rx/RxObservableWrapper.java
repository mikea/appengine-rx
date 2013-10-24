package com.mikea.gae.rx;

import com.mikea.gae.rx.base.IDisposable;
import com.mikea.gae.rx.base.IObservable;
import com.mikea.gae.rx.base.IObserver;

/**
 * @author mike.aizatsky@gmail.com
 */
class RxObservableWrapper<T> extends RxStream<T> {
    private final IObservable<T> observable;

    RxObservableWrapper(Rx rx, IObservable<T> observable) {
        super(rx);
        this.observable = observable;
    }

    public static <T> RxStream<T> wrap(Rx rx, IObservable<T> src) {
        return new RxObservableWrapper<>(rx, src);
    }

    @Override
    public IDisposable subscribe(IObserver<T> observer) {
        return observable.subscribe(observer);
    }
}
