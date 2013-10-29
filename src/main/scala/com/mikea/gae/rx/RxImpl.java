package com.mikea.gae.rx;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mikea.gae.rx.base.DoFn;
import com.mikea.gae.rx.base.EmitFn;
import com.mikea.gae.rx.base.IObserver;
import com.mikea.util.Loggers;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

@Singleton
class RxImpl implements Rx {
    private static final Logger log = Loggers.getContextLogger();

    private final Set<RxPipeline> pipelines;
    private final Injector injector;
    private final BlobstoreService blobstoreService;
    private boolean initialized;
    private RxPushStream<RxHttpRequestEvent> requestsStream = new RxPushStream<>(this);
    private RxPushStream<RxInitializationEvent> initializationStream = new RxPushStream<>(this);

    @Inject
    RxImpl(Set<RxPipeline> pipelines,
           Injector injector,
           BlobstoreService blobstoreService) {
        this.pipelines = pipelines;
        this.injector = injector;
        this.blobstoreService = blobstoreService;
    }

    static String getCronUrl(String cronSpecification) {
        return RxModule.RX_CRON_URL_BASE + cronSpecification.replaceAll(" ", "_");
    }

    static String getUploadsUrl() {
        return RxModule.RX_UPLOADS_BASE;
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
        return requests().filter(new Predicate<RxHttpRequestEvent>() {
            @Override
            public boolean apply(RxHttpRequestEvent input) {
                return input.request.getRequestURI().equals(getCronUrl(specification));
            }
        }).transform(new Function<RxHttpRequestEvent, RxCronEvent>() {
            @Override
            public RxCronEvent apply(RxHttpRequestEvent input) {
                return new RxCronEvent();
            }
        });
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

    @Override
    public RxStream<RxUploadEvent> uploads() {
        return requests().filter(new Predicate<RxHttpRequestEvent>() {
            @Override
            public boolean apply(RxHttpRequestEvent input) {
                return input.request.getRequestURI().equals(getUploadsUrl());
            }
        }).transform(new Function<RxHttpRequestEvent, RxUploadEvent>() {
            @Override
            public RxUploadEvent apply(RxHttpRequestEvent input) {
                Map<String,List<BlobInfo>> blobInfos = blobstoreService.getBlobInfos(input.request);
                return new RxUploadEvent(input, blobInfos);
            }
        });
    }

    @Override
    public <T extends Serializable> IObserver<RxTask<T>> taskqueue(final String queueName) {
        return new IObserver<RxTask<T>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Exception e) {
                throw new UnsupportedOperationException(e);
            }

            @Override
            public void onNext(RxTask<T> value) throws IOException {
                RxTasks.enqueue(queueName, value);
            }
        };
    }

    @Override
    public <T extends Serializable> RxStream<RxTask<T>> tasks(final String queueName, Class<T> payloadClass) {
        return tasks(queueName, TypeToken.of(payloadClass));
    }

    @Override
    public <T extends Serializable> RxStream<RxTask<T>> tasks(final String queueName, TypeToken<T> typeToken) {
        return requests().filter(new Predicate<RxHttpRequestEvent>() {
            @Override
            public boolean apply(@Nullable RxHttpRequestEvent input) {
                HttpServletRequest request = input.request;
                String requestQueueName = request.getHeader("X-AppEngine-QueueName");
                return Objects.equals(requestQueueName, queueName);
            }
        }).transform(new Function<RxHttpRequestEvent, RxTask<T>>() {
            @Override
            public RxTask<T> apply(RxHttpRequestEvent input) {
                try {
                    input.sendOk();
                    return RxTask.fromRequest(input.request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public RxStream<RxVersionUpdateEvent> updates() {
        return initialized().transform(new DoFn<RxInitializationEvent, RxVersionUpdateEvent>() {
            @Override
            public void process(RxInitializationEvent rxInitializationEvent, EmitFn<RxVersionUpdateEvent> emitFn) throws IOException {
                log.info("Checking version...");
                log.info(System.getProperties().toString());
            }
        });
    }

    @Override
    public RxStream<RxInitializationEvent> initialized() {
        return initializationStream;
    }


    public RxStream<RxHttpRequestEvent> requests() {
        return requestsStream;
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        initIfNeeded();
        log.fine(String.format("handleRequest(%s)", request.getRequestURI()));
        RxHttpResponse rxResponse = new RxHttpResponse(response);
        requestsStream.onNext(new RxHttpRequestEvent(this, request, rxResponse));

        if (!rxResponse.hasResponse()) {
            rxResponse.sendError(404, request.getRequestURI());
            log.fine("Error 404 : request not processed");
        }
    }

    public void onContextInitialized() throws IOException {
        initIfNeeded();
        log.fine("onContextInitialized");
        initializationStream.onNext(new RxInitializationEvent());
    }
}
