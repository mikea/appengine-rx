package com.mikea.gae.rx

import com.mikea.gae.rx.base.Observable
import java.io.Serializable

import scala.reflect.runtime.universe._
import com.mikea.gae.rx.tasks.RxTasksFactory

/**
 * @author mike.aizatsky@gmail.com
 */
trait Rx {
  def requests(pattern: String): Observable[RxHttpRequestEvent]

  def cron(specification: String): Observable[RxCronEvent]

  def appVersionUpdate(): Observable[RxVersionUpdateEvent]

  def contextInitialized(): Observable[RxInitializationEvent]

  def upload(): Observable[RxUploadEvent]

  def taskqueue(queueName: String): Observable[RxHttpRequestEvent]

  def tasks : RxTasksFactory
}