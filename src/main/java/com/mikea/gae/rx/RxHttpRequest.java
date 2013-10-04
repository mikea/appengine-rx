package com.mikea.gae.rx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxHttpRequest {
    private final Rx rx;
    public final HttpServletRequest request;
    private final HttpServletResponse response;

    public RxHttpRequest(Rx rx, HttpServletRequest request, HttpServletResponse response) {
        this.rx = rx;
        this.request = request;
        this.response = response;
    }
}
