package com.mikea.gae.rx;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mikea.util.Loggers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Singleton
class RxServlet extends HttpServlet {
    private final RxImpl rx;

    @Inject
    public RxServlet(RxImpl rx) {
        this.rx = rx;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        rx.handleRequest(request, response);
    }
}
