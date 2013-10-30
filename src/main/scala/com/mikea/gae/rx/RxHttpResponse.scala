package com.mikea.gae.rx

import javax.servlet.http.HttpServletResponse

/**
 * @author mike.aizatsky@gmail.com
 */
class RxHttpResponse(response: HttpServletResponse) {
  private var _hasResponse: Boolean = false

  def hasResponse = _hasResponse

  def sendRedirect(url: String): Unit = {
    _hasResponse = true
    response.sendRedirect(url)
  }

  def sendError(errorCode: Int, message: String): Unit = {
    _hasResponse = true
    response.sendError(errorCode, message)
  }

  def sendOk(): Unit = {
    _hasResponse = true
    response.sendError(200)
  }

}