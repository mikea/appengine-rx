package com.mikea.gae.rx

import com.google.inject.Inject
import com.google.inject.Singleton
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Singleton class RxServlet @Inject() (rx : RxImpl) extends HttpServlet {
  protected override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    handleRequest(req, resp)
  }

  protected override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    handleRequest(req, resp)
  }

  private def handleRequest(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    rx.handleRequest(request, response)
  }
}