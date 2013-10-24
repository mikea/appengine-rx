package com.mikea.gae.rx;

import com.mikea.gae.rx.base.Observables;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxStreams {
    public static <T> RxStream<T> flatten(RxStream<Iterable<T>> stream) {
        return stream.wrap(Observables.flatten(stream));
    }
}
