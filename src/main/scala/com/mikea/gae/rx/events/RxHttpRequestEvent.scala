package com.mikea.gae.rx.events

import javax.servlet.http.HttpServletRequest
import com.mikea.gae.rx.{RxHttpResponse, Rx}

/**
 * @author mike.aizatsky@gmail.com
 */
class RxHttpRequestEvent(rx: Rx, _request: HttpServletRequest, _response: RxHttpResponse) {
  def sendRedirect(url: String): Unit = {
    response.sendRedirect(url)
  }

  def sendError(i: Int, message: String): Unit = {
    response.sendError(i, message)
  }

  def sendOk(): Unit = {
    response.sendOk()
  }

  def request = _request
  def response = _response
}