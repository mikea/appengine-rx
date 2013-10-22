package com.mikea.gae.rx;

import com.google.common.reflect.TypeToken;
import com.google.inject.Injector;

import java.io.Serializable;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface Rx {
    RxStream<RxCronEvent> cron(String specification);

    Injector getInjector();

    RxStream<RxUploadEvent> uploads();

    <T extends Serializable> IObserver<RxTask<T>> taskqueue(String queueName);

    <T extends Serializable> RxStream<RxTask<T>> tasks(String queueName, Class<T> payloadClass);
    <T extends Serializable> RxStream<RxTask<T>> tasks(String queueName, TypeToken<T> typeToken);
}
