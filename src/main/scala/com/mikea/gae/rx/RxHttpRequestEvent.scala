package com.mikea.gae.rx

import javax.servlet.http.HttpServletRequest

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