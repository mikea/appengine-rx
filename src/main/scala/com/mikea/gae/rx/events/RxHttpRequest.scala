package com.mikea.gae.rx.events

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.mikea.gae.rx.Rx

/**
 * @author mike.aizatsky@gmail.com
 */
class RxHttpRequest(val httpRequest: HttpServletRequest, val httpResponse: HttpServletResponse) extends RxEvent {
  private[rx] var processed : Boolean = false

  def this(request: RxHttpRequest) = this(request.httpRequest, request.httpResponse)
}