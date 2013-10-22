package com.mikea.gae.rx;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxStreams {
    public static <T> RxStream<T> flatten(RxStream<Iterable<T>> stream) {
        return stream.wrap(Observables.flatten(stream));
    }
}
