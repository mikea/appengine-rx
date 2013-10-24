package com.mikea.gae.rx.base;

import java.io.IOException;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface DoFn<S, T> {
    void process(S s, EmitFn<T> emitFn) throws IOException;

}
