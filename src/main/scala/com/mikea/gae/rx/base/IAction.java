package com.mikea.gae.rx.base;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface IAction<T> {
    void perform(T t);
}
