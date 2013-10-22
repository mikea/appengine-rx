package com.mikea.gae.rx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RxPushStream<T> extends RxStream<T> {
    private List<IObserver<T>> observers = new ArrayList<>();

    protected RxPushStream(Rx rx) {
        super(rx);
    }

    public synchronized void onNext(T t) throws IOException {
        for (IObserver<T> observer : observers) {
            observer.onNext(t);
        }
    }

    @Override
    public synchronized IDisposable subscribe(IObserver<T> observer) {
        observers.add(observer);
        return new IDisposable() {
            @Override
            public void dispose() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
