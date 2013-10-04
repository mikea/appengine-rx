package com.mikea.gae.rx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mike.aizatsky@gmail.com
 */

abstract class RxHandler {
    public abstract boolean handleRequest(HttpServletRequest request, HttpServletResponse response);
}
