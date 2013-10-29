package com.mikea.gae.rx.base;

public interface IObservable<T> {
    IDisposable subscribe(IObserver<T> observer);
}
