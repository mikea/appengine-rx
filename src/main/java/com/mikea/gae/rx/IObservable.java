package com.mikea.gae.rx;

public interface IObservable<T> {
    IDisposable subscribe(IObserver<T> observer);
}
