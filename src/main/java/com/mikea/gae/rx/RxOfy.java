package com.mikea.gae.rx;

import com.google.common.base.Function;
import com.googlecode.objectify.Key;
import com.mikea.gae.rx.base.IAction;

import javax.annotation.Nullable;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxOfy {
    private RxOfy() { }

    public static <T> IAction<T> save() {
        return new IAction<T>() {
            @Override
            public void perform(T t) {
                ofy().save().entity(t);
            }
        };
    }

    public static <T> Function<Key<T>, T> loadSafe() {
        return new Function<Key<T>, T>() {
            @Nullable
            @Override
            public T apply(@Nullable Key<T> input) {
                return ofy().load().key(input).safe();
            }
        };
    }
}
