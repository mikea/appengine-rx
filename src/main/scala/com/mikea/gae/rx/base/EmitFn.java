package com.mikea.gae.rx.base;

import java.io.IOException;

/**
* @author mike.aizatsky@gmail.com
*/
public interface EmitFn<T> {
    void emit(T t) throws IOException;
}
