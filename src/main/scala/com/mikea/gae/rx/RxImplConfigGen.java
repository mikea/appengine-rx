package com.mikea.gae.rx;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mikea.gae.rx.base.IDisposable;
import com.mikea.gae.rx.base.IObserver;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

class RxImplConfigGen implements Rx {
    private final Injector injector;
    private Set<String> cronSpecifications = new HashSet<>();
    private Set<String> taskQueues = new HashSet<>();

    @Inject
    RxImplConfigGen(Injector injector) {
        this.injector = injector;
    }

    @Override
    public RxStream<RxCronEvent> cron(String specification) {
        cronSpecifications.add(specification);
        return new RxConfigGenStream<>(this);
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    @Override
    public RxStream<RxUploadEvent> uploads() {
        return new RxConfigGenStream<>(this);
    }

    @Override
    public <T extends Serializable> IObserver<RxTask<T>> taskqueue(String queueName) {
        taskQueues.add(queueName);

        return new IObserver<RxTask<T>>() {
            @Override
            public void onCompleted() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onError(Exception e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onNext(RxTask<T> value) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public <T extends Serializable> RxStream<RxTask<T>> tasks(String uploads, Class<T> payloadClass) {
        return new RxConfigGenStream<>(this);
    }

    @Override
    public <T extends Serializable> RxStream<RxTask<T>> tasks(String queueName, TypeToken<T> typeToken) {
        return new RxConfigGenStream<>(this);
    }

    @Override
    public RxStream<RxVersionUpdateEvent> updates() {
        return new RxConfigGenStream<>(this);
    }

    @Override
    public RxStream<RxInitializationEvent> initialized() {
        return new RxConfigGenStream<>(this);
    }

    public void generateConfigs() {
        System.out.println("----- cron.xml -----");
        System.out.println(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<cronentries>");
        for (String cronSpecification : cronSpecifications) {
            System.out.println(
                    "    <cron>\n" +
                            "        <url>" + RxImpl.getCronUrl(cronSpecification) + "</url>\n" +
                            "        <schedule>" + cronSpecification + "</schedule>\n" +
                            "    </cron>");
        }
        System.out.println("</cronentries>\n");
        System.out.println("----- queue.xml -----");
        System.out.println(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<queue-entries>");
        for (String queue : taskQueues) {
            System.out.println(
                    "    <queue>\n" +
                            "        <name>" + queue + "</name>\n" +
                            "    </queue>");
        }
        System.out.println("</queue-entries>\n");
    }

    private static class RxConfigGenStream<T> extends RxStream<T> {
        protected RxConfigGenStream(Rx rx) {
            super(rx);
        }

        @Override
        public IDisposable subscribe(IObserver<T> observer) {
            return new IDisposable() {
                @Override
                public void dispose() {
                }
            };
        }
    }
}
