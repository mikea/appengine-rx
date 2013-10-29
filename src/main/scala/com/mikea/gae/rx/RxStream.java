package com.mikea.gae.rx;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.mikea.gae.rx.base.DoFn;
import com.mikea.gae.rx.base.IAction;
import com.mikea.gae.rx.base.IObservable;
import com.mikea.gae.rx.base.IObserver;
import com.mikea.gae.rx.base.Observables;
import com.mikea.gae.rx.base.Observers;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class RxStream<T> implements IObservable<T> {
    private final Rx rx;

    @Inject
    protected RxStream(Rx rx) {
        this.rx = rx;
    }

    public final <U> RxStream<U> transform(
            Class<? extends Function<T, U>> fnClass) {
        return wrap(Observables.transform(this, fnClass, rx.getInjector()));
    }

    public final <NewEventType> RxStream<NewEventType> transform(
            Function<T, NewEventType> fn) {
        return wrap(Observables.transform(this, fn));
    }

    public final <NewEventType> RxStream<NewEventType> transform(
            DoFn<T, NewEventType> fn) {
        return wrap(Observables.transform(this, fn));
    }

    public final void apply(Class<? extends IAction<T>> actionClass) {
        Observables.apply(this, actionClass, rx.getInjector());
    }

    public final RxStream<T> apply(IAction<T> action) {
        return RxObservableWrapper.wrap(rx, Observables.apply(this, action));
    }

    <T> RxStream<T> wrap(IObservable<T> src) {
        return RxObservableWrapper.wrap(rx, src);
    }

    public final RxStream<T> filter(Predicate<T> predicate) {
        return wrap(Observables.filter(this, predicate));
    }

    public final void sink(IObserver<T> sink) {
        Observables.sink(this, sink);
    }

    public final void sink(IAction<T> sink) {
        Observables.sink(this, Observers.asObserver(sink));
    }

    public final <U> RxStream<U> transformMany(Class<? extends Function<T, Iterable<U>>> fnClass) {
        return wrap(Observables.transformMany(this, fnClass, rx.getInjector()));
    }
}
