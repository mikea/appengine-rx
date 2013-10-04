package com.mikea.gae.rx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class RxCronHandler extends RxHandler {
    public static final String RX_CRON_URL_BASE = RxModule.RX_URL_BASE + "/cron/";
    private final String specification;
    private RxPushStream<RxCronEvent> stream;

    public RxCronHandler(Rx rx, String specification) {
        this.specification = specification;
        stream = new RxPushStream<>(rx);
    }

    static String getUrl(String cronSpecification) {
        return RX_CRON_URL_BASE + cronSpecification.replaceAll(" ", "_");
    }

    public RxStream<RxCronEvent> getStream() {
        return stream;
    }

    @Override
    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String requestURI = request.getRequestURI();

        if (!getUrl(specification).equals(requestURI)) {
            return false;
        }

        stream.onNext(new RxCronEvent());
        return true;
    }
}
