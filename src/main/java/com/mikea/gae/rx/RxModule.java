package com.mikea.gae.rx;

import com.google.inject.servlet.ServletModule;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxModule extends ServletModule {
    public static final String RX_URL_BASE = "/_rx";

    @Override
    protected void configureServlets() {
        super.configureServlets();
        serve(RX_URL_BASE + "*").with(RxServlet.class);
    }
}
