package com.mikea.gae.rx;

import com.google.common.base.Function;
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
            Class<? extends Function<EventType, NewEventType>> processFeedsClass) {
        return wrap(Observables.transform(this, processFeedsClass, rx.getInjector()));
    }

    public void apply(Class<? extends IAction<EventType>> actionClass) {
        Observables.apply(this, actionClass, rx.getInjector());
    }

    private <T> RxStream<T> wrap(IObservable<T> src) {
        return RxObservableWrapper.wrap(rx, src);
    }
}
