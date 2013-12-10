package com.mikea.gae.rx.impl

import com.google.inject.Inject
import com.google.inject.Injector
import com.mikea.gae.rx.base.{Subject, Observable, Observer}
import java.io.Serializable
import scala.collection.immutable.HashSet
import scala.reflect.runtime.universe._
import com.mikea.gae.rx.tasks.RxTask
import com.mikea.gae.rx._
import com.mikea.gae.rx.impl.RxImplConfigGen.RxConfigGenStream

private[rx] object RxImplConfigGen {
  class RxConfigGenStream[T] (_injector: Injector) extends Observable[T] {
    def subscribe(observer: Observer[T]) = {
      throw new UnsupportedOperationException()
    }

    def instantiate[C](aClass: Class[C]) = _injector.getInstance(aClass)
  }
}

private[rx] class RxImplConfigGen @Inject() (_injector: Injector) extends Rx {
  def cron(specification: String): Observable[RxCronEvent] = {
    cronSpecifications += specification
    new RxConfigGenStream[RxCronEvent](this.injector)
  }

  def injector = _injector

  def upload(): Observable[RxUploadEvent] = {
    new RxImplConfigGen.RxConfigGenStream[RxUploadEvent](this.injector)
  }

  def tasks[T <: Serializable : TypeTag](queueName: String): Subject[RxTask[T]] = ???

  def appVersionUpdate(): Observable[RxVersionUpdateEvent] = {
    new RxImplConfigGen.RxConfigGenStream[RxVersionUpdateEvent](this.injector)
  }

  def contextInitialized(): Observable[RxInitializationEvent] = {
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

  def requests(pattern: String) = ???

  def taskqueue(queueName: String) = ???

  def tasks = ???
}