package com.mikea.gae.rx;

import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.HashSet;
import java.util.Set;

class RxImplConfigGen implements Rx {
    private final Injector injector;
    private Set<String> cronSpecifications = new HashSet<>();

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

    public void generateConfigs() {
        System.out.println(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<cronentries>");
        for (String cronSpecification : cronSpecifications) {
            System.out.println(
                    "    <cron>\n" +
                            "        <url>" + RxCronHandler.getUrl(cronSpecification) + "</url>\n" +
                            "        <schedule>" + cronSpecification + "</schedule>\n" +
                            "    </cron>");
        }
        System.out.println("</cronentries>\n");
    }

    private static class RxConfigGenStream<T> extends RxStream<T> {
        protected RxConfigGenStream(Rx rx) {
            super(rx);
        }

        @Override
        public IDisposable subscribe(IObserver<T> observer) {
            throw new UnsupportedOperationException();
        }
    }
}
