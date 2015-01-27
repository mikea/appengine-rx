package com.mikea.gae.rx

import com.mikea.gae.rx.base.Transformer
import com.mikea.gae.rx.events.RxHttpRequest
import javax.servlet.http.HttpServletResponse

abstract class RxHttpResponse(val request : RxHttpRequest) {
  protected def render(httpResponse : HttpServletResponse): Unit

  private[rx] def render() : Unit = {
    render(request.httpResponse)
    request.processed = true
  }
}

case class RxHttpErrorResponse(errorCode: Int, message: String, override val request : RxHttpRequest) extends RxHttpResponse(request) {
  protected def render(httpResponse: HttpServletResponse) = httpResponse.sendError(errorCode, message)
}

case class RxHttpOkResponse(body : String, override val request : RxHttpRequest) extends RxHttpResponse(request) {
  def this(request : RxHttpRequest) = this(null, request)

  protected def render(httpResponse: HttpServletResponse) = {
    httpResponse.setStatus(200)
    if (body != null) {
      httpResponse.getOutputStream.print(body)
    }
  }
}
case class RxHttpRedirectResponse(url: String, override val request : RxHttpRequest) extends RxHttpResponse(request) {
  protected def render(httpResponse: HttpServletResponse) = httpResponse.sendRedirect(url)
}

object RxHttpResponse {
  def ok[Event <: RxHttpRequest](body : String) : Transformer[Event, RxHttpResponse] = Transformer.map((evt: Event) => new RxHttpOkResponse(body, evt))
  def ok[Event <: RxHttpRequest]() : Transformer[Event, RxHttpResponse] = Transformer.map((evt: Event) => new RxHttpOkResponse(evt))
}