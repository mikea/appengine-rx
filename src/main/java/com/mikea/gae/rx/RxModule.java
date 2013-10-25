package com.mikea.gae.rx;

import com.google.inject.servlet.ServletModule;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxModule extends ServletModule {
    public static final String RX_URL_BASE = "/_rx";
    public static final String RX_CRON_URL_BASE = RxModule.RX_URL_BASE + "/cron/";

    @Override
    protected void configureServlets() {
        super.configureServlets();
        serve(RX_URL_BASE + "*").with(RxServlet.class);
        bind(Rx.class).to(RxImpl.class);
    }
}
