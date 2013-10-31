package com.mikea.gae.rx

import com.mikea.gae.rx.base.{Subject, Observable, Observer}
import java.io.Serializable

import scala.reflect.runtime.universe._

/**
 * @author mike.aizatsky@gmail.com
 */
trait Rx {
  def requests(pattern: String): Observable[RxHttpRequestEvent]

  def cron(specification: String): Observable[RxCronEvent]

  def appVersionUpdate(): Observable[RxVersionUpdateEvent]

  def contextInitialized(): Observable[RxInitializationEvent]

  def upload(): Observable[RxUploadEvent]

  def taskqueue[T <: Serializable : TypeTag](queueName: String): Subject[RxTask[T]]
}