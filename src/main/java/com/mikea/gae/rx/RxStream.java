package com.mikea.gae.rx;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class RxStream<EventType> implements IObservable<EventType> {
    private final Rx rx;

    @Inject
    protected RxStream(Rx rx) {
        this.rx = rx;
    }

    public final <NewEventType> RxStream<NewEventType> transform(
            Class<? extends Function<EventType, NewEventType>> fnClass) {
        return wrap(Observables.transform(this, fnClass, rx.getInjector()));
    }

    public final <NewEventType> RxStream<NewEventType> transform(
            Function<EventType, NewEventType> fn) {
        return wrap(Observables.transform(this, fn));
    }

    public void apply(Class<? extends IAction<EventType>> actionClass) {
        Observables.apply(this, actionClass, rx.getInjector());
    }

    private <T> RxStream<T> wrap(IObservable<T> src) {
        return RxObservableWrapper.wrap(rx, src);
    }

    public RxStream<EventType> filter(Predicate<EventType> predicate) {
        return wrap(Observables.filter(this, predicate));
    }
}
