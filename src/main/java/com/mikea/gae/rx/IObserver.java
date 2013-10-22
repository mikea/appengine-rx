package com.mikea.gae.rx;

import java.io.IOException;

public interface IObserver<T> {
    void onCompleted();
    void onError(Exception e);
    void onNext(T value) throws IOException;
}
