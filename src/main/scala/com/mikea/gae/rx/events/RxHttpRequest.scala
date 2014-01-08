package com.mikea.gae.rx.events

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.mikea.gae.rx.Rx

/**
 * @author mike.aizatsky@gmail.com
 */
class RxHttpRequest(val rx: Rx, val httpRequest: HttpServletRequest, val httpResponse: HttpServletResponse)