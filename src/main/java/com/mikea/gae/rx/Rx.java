package com.mikea.gae.rx;

import com.google.inject.Injector;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface Rx {
    public RxStream<RxCronEvent> cron(String specification);

    Injector getInjector();
}
