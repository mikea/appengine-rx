package com.mikea.gae.rx;

import com.google.common.base.Function;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;

import javax.annotation.Nullable;
import java.util.Map;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxOfy {
    private RxOfy() { }

    public static <T> Function<T, Result<Key<T>>> save() {
        return new Function<T, Result<Key<T>>>() {
            @Override
            public Result<Key<T>> apply(T input) {
                return ofy().save().entity(input);
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

    public static <T> Function<Iterable<T>, Result<Map<Key<T>, T>>> saveMulti() {
        return new Function<Iterable<T>, Result<Map<Key<T>, T>>>() {
            @Override
            public Result<Map<Key<T>, T>> apply(@Nullable Iterable<T> values) {
                return ofy().save().entities(values);
            }
        };
    }
}
