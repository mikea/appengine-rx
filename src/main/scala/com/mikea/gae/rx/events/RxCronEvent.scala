package com.mikea.gae.rx.events

import com.mikea.gae.rx.Rx
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author mike.aizatsky@gmail.com
 */
class RxCronEvent(rx: Rx, request: HttpServletRequest, response: HttpServletResponse) extends RxHttpRequest(rx, request, response) {
}