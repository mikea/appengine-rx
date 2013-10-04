package com.mikea.gae.rx;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@Singleton
class RxImpl implements Rx {
    private final Set<RxPipeline> pipelines;
    private final Injector injector;
    private boolean initialized;
    private RxPushStream<RxHttpRequest> requestsStream = new RxPushStream<>(this);

    @Inject
    RxImpl(Set<RxPipeline> pipelines, Injector injector) {
        this.pipelines = pipelines;
        this.injector = injector;
    }

    static String getUrl(String cronSpecification) {
        return RxModule.RX_CRON_URL_BASE + cronSpecification.replaceAll(" ", "_");
    }

    private synchronized void initIfNeeded() {
        if (initialized) return;
        initialized = true;
        for (RxPipeline pipeline : pipelines) {
            pipeline.init(this);
        }
    }

    @Override
    public RxStream<RxCronEvent> cron(final String specification) {
        return requests().filter(new Predicate<RxHttpRequest>() {
            @Override
            public boolean apply(RxHttpRequest input) {
                return input.request.getRequestURI().equals(getUrl(specification));
            }
        }).transform(new Function<RxHttpRequest, RxCronEvent>() {
            @Override
            public RxCronEvent apply(RxHttpRequest input) {
                return new RxCronEvent();
            }
        });
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        initIfNeeded();
        requestsStream.onNext(new RxHttpRequest(this, request, response));
    }

    public RxStream<RxHttpRequest> requests() {
        return requestsStream;
    }
}
