package com.mikea.gae.rx.impl

import javax.servlet._
import com.google.inject.{Singleton, Inject}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author mike.aizatsky@gmail.com
 */
@Singleton private[rx] class RxFilter  @Inject() (rx : RxImpl) extends Filter {
  def destroy(): Unit = rx.destroy()

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    rx.doFilter(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
  }

  def init(filterConfig: FilterConfig): Unit = rx.init()
}
