package com.mikea.gae.rx;

import com.google.common.base.Optional;
import com.mikea.gae.rx.base.DoFn;
import com.mikea.gae.rx.base.EmitFn;
import com.mikea.gae.rx.base.IObserver;

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

    public static <T> DoFn<Optional<T>, T> skipAbsent() {
        return new DoFn<Optional<T>, T>() {
            @Override
            public void process(Optional<T> in, EmitFn<T> emitFn) throws IOException {
                if (in.isPresent()) {
                    emitFn.emit(in.get());
                }
            }
        };
    }

    public static <T> DoFn<Iterable<T>, T> flatten() {
        return new DoFn<Iterable<T>, T>() {
            @Override
            public void process(Iterable<T> ts, EmitFn<T> emitFn) throws IOException {
                for (T t : ts) {
                    emitFn.emit(t);
                }
            }
        };
    }
}
