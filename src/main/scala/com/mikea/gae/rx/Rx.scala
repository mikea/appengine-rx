package com.mikea.gae.rx

import com.mikea.gae.rx.base._

import com.mikea.gae.rx.events._
import com.google.appengine.api.taskqueue.TaskOptions

/**
 * @author mike.aizatsky@gmail.com
 */
trait Rx {
  def appVersionUpdate(): Observable[RxVersionUpdateEvent]

  def contextInitialized(): Observable[RxInitializationEvent]

  def requests(pattern: String): TransformerSlot[RxHttpRequest, RxHttpResponse]

  def cron(specification: String): TransformerSlot[RxCronEvent, RxHttpResponse]

  def upload(): TransformerSlot[RxUploadEvent, RxHttpResponse]

  def taskqueue(queueName: String): Transformer[TaskOptions, RxHttpRequest]
}                         `