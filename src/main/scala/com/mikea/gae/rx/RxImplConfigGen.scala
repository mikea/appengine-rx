package com.mikea.gae.rx

import com.google.common.reflect.TypeToken
import com.google.inject.Inject
import com.google.inject.Injector
import com.mikea.gae.rx.base.{IObservable, IObserver}
import java.io.Serializable
import com.mikea.gae.rx.RxImplConfigGen.RxConfigGenStream
import scala.collection.immutable.HashSet

object RxImplConfigGen {
  class RxConfigGenStream[T] (_injector: Injector) extends IObservable[T] {
    def subscribe(observer: IObserver[T]) = {
      throw new UnsupportedOperationException()
    }

    def instantiate[C](aClass: Class[C]) = _injector.getInstance(aClass)
  }
}

class RxImplConfigGen @Inject() (_injector: Injector) extends Rx {
  def cron(specification: String): IObservable[RxCronEvent] = {
    cronSpecifications += specification
    new RxConfigGenStream[RxCronEvent](this.injector)
  }

  def injector = _injector

  def uploads: IObservable[RxUploadEvent] = {
    new RxImplConfigGen.RxConfigGenStream[RxUploadEvent](this.injector)
  }

  def taskqueue[T <: Serializable](queueName: String): IObserver[RxTask[T]] = {
    taskQueues += queueName
    new IObserver[RxTask[T]] {
      def onCompleted(): Unit = {
        throw new UnsupportedOperationException
      }

      def onError(e: Exception): Unit = {
        throw new UnsupportedOperationException
      }

      def onNext(value: RxTask[T]): Unit = {
        throw new UnsupportedOperationException
      }
    }
  }

  def tasks[T <: Serializable](uploads: String, payloadClass: Class[T]): IObservable[RxTask[T]] = {
    new RxImplConfigGen.RxConfigGenStream[RxTask[T]](this.injector)
  }

  def tasks[T <: Serializable](queueName: String, typeToken: TypeToken[T]): IObservable[RxTask[T]] = {
    new RxImplConfigGen.RxConfigGenStream[RxTask[T]](this.injector)
  }

  def updates: IObservable[RxVersionUpdateEvent] = {
    new RxImplConfigGen.RxConfigGenStream[RxVersionUpdateEvent](this.injector)
  }

  def initialized: IObservable[RxInitializationEvent] = {
    new RxImplConfigGen.RxConfigGenStream[RxInitializationEvent](injector)
  }

  def generateConfigs(): Unit = {
    System.out.println("----- cron.xml -----")
    System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<cronentries>")
    for (cronSpecification <- cronSpecifications) {
      System.out.println(s"    <cron>\n        <url>${RxImpl.getCronUrl(cronSpecification)}</url>\n        <schedule>$cronSpecification</schedule>\n    </cron>")
    }
    System.out.println("</cronentries>\n")
    System.out.println("----- queue.xml -----")
    System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<queue-entries>")
    for (queue <- taskQueues) {
      System.out.println(s"    <queue>\n        <name>$queue</name>\n    </queue>")
    }
    System.out.println("</queue-entries>\n")
  }

  private var cronSpecifications: Set[String] = new HashSet[String]
  private var taskQueues: Set[String] = new HashSet[String]
}