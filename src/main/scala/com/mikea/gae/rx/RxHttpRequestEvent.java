package com.mikea.gae.rx;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxHttpRequestEvent {
    private final Rx rx;
    public final HttpServletRequest request;
    private final RxHttpResponse response;

    protected RxHttpRequestEvent(RxHttpRequestEvent event) {
        this(event.rx, event.request, event.response);
    }

    public RxHttpRequestEvent(Rx rx, HttpServletRequest request, RxHttpResponse response) {
        this.rx = rx;
        this.request = request;
        this.response = response;
    }

    public void sendRedirect(String url) throws IOException {
        response.sendRedirect(url);
    }


    public void sendError(int i, String message) throws IOException {
        response.sendError(i, message);
    }

    public void sendOk() throws IOException {
        response.sendOk();
    }
}
