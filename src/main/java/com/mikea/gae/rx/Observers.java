package com.mikea.gae.rx;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Observers {
    public static <T> IObserver<T> asObserver(final IAction<T> action) {
        return new IObserver<T>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Exception e) {
                throw new UnsupportedOperationException(e);
            }

            @Override
            public void onNext(T value) {
                action.perform(value);
            }
        };
    }
}
