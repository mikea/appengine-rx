package com.mikea.gae.rx;

public interface IObserver<T> {
    void onCompleted();
    void onError(Exception e);
    void onNext(T value);
}
