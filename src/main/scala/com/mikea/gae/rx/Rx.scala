package com.mikea.gae.rx

import com.google.common.reflect.TypeToken
import com.google.inject.Injector
import com.mikea.gae.rx.base.IObservable
import com.mikea.gae.rx.base.IObserver
import java.io.Serializable

/**
 * @author mike.aizatsky@gmail.com
 */
trait Rx {
  def cron(specification: String): IObservable[RxCronEvent]

  def appVersionUpdate(): IObservable[RxVersionUpdateEvent]

  def contextInitialized(): IObservable[RxInitializationEvent]

  def upload(): IObservable[RxUploadEvent]

  def tasks[T <: Serializable](queueName: String, payloadClass: Class[T]): IObservable[RxTask[T]]

  def tasks[T <: Serializable](queueName: String, typeToken: TypeToken[T]): IObservable[RxTask[T]]

  def taskqueue[T <: Serializable](queueName: String): IObserver[RxTask[T]]
}