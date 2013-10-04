package com.mikea.gae.rx;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

@Singleton
class RxImpl implements Rx {
    private final Set<RxPipeline> pipelines;
    private final Injector injector;
    private final Set<RxHandler> handlers = new HashSet<>();
    private boolean initialized;

    @Inject
    RxImpl(Set<RxPipeline> pipelines, Injector injector) {
        this.pipelines = pipelines;
        this.injector = injector;
    }

    private void init() {
        for (RxPipeline pipeline : pipelines) {
            pipeline.init(this);
        }
    }

    @Override
    public RxStream<RxCronEvent> cron(String specification) {
        RxCronHandler handler = new RxCronHandler(this, specification);
        handlers.add(handler);
        return handler.getStream();
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        initIfNeeded();

        for (RxHandler handler : handlers) {
            if (handler.handleRequest(request, response)) {
                return;
            }
        }

        throw new IllegalStateException("Handler not found");
    }

    private synchronized void initIfNeeded() {
        if (initialized) return;
        initialized = true;
        init();
    }
}
