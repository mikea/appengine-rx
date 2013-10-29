package com.mikea.gae.rx;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Set;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxMainImpl {
    private final Set<RxPipeline> pipelines;
    private final RxImplConfigGen rx;

    @Inject
    public RxMainImpl(Set<RxPipeline> pipelines, RxImplConfigGen configGen) {
        this.pipelines = pipelines;
        this.rx = configGen;
    }

    public static void main(Injector injector, String[] args) {
        injector.getInstance(RxMainImpl.class).run(args);
    }

    private void run(String[] args) {
        for (RxPipeline pipeline : pipelines) {
            pipeline.init(rx);
        }

        rx.generateConfigs();
    }
}
