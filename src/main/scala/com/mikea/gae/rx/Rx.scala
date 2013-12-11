package com.mikea.gae.rx

import com.mikea.gae.rx.base.{Transformer, Observable}

import com.mikea.gae.rx.tasks.RxTasksFactory
import com.mikea.gae.rx.events._
import com.google.appengine.api.taskqueue.TaskOptions

/**
 * @author mike.aizatsky@gmail.com
 */
trait Rx {
  def requests(pattern: String): Observable[RxHttpRequestEvent]

  def cron(specification: String): Observable[RxCronEvent]

  def appVersionUpdate(): Observable[RxVersionUpdateEvent]

  def contextInitialized(): Observable[RxInitializationEvent]

  def upload(): Observable[RxUploadEvent]

  def taskqueue(queueName: String): Transformer[TaskOptions, RxHttpRequestEvent]

  def tasks : RxTasksFactory
}