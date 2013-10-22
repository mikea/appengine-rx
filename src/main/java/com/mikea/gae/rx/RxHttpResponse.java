package com.mikea.gae.rx;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxHttpResponse {
    private final HttpServletResponse response;
    private boolean hasResponse;

    public RxHttpResponse(HttpServletResponse response) {
        this.response = response;
    }

    public boolean hasResponse() {
        return hasResponse;
    }

    public void sendRedirect(String url) throws IOException {
        hasResponse = true;
        response.sendRedirect(url);
    }

    public void sendError(int errorCode, String message) throws IOException {
        hasResponse = true;
        response.sendError(errorCode, message);

    }

    public void sendOk() throws IOException {
        hasResponse = true;
        response.sendError(200);
    }
}
