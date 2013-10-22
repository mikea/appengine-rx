package com.mikea.gae.rx;

import java.io.IOException;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxUtils {
    public static <T extends RxHttpRequestEvent> IObserver<T> redirect(final String url) {
        return new IObserver<T>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Exception e) {
            }

            @Override
            public void onNext(T value) throws IOException {
                value.sendRedirect(url);
            }
        };
    }
}
