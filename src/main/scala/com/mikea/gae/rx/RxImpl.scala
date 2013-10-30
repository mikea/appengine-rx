package com.mikea.gae.rx

import com.google.appengine.api.blobstore.{BlobInfo, BlobstoreService}
import com.google.common.reflect.TypeToken
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import com.mikea.util.Loggers
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.Objects
import java.util.logging.Logger
import com.mikea.gae.rx.base.{IObservable, DoFn, Observers, IObserver}
import java.util

@Singleton object RxImpl {
  private[rx] def getCronUrl(cronSpecification: String): String = s"${RxUrls.RX_CRON_URL_BASE}${cronSpecification.replaceAll(" ", "_")}"

  private[rx] def getUploadsUrl: String = RxUrls.RX_UPLOADS_BASE

  private final val log: Logger = Loggers.getContextLogger
}

@Singleton class RxImpl @Inject()(_pipelines: Set[RxPipeline], _injector: Injector, blobstoreService: BlobstoreService) extends Rx {

  private def initIfNeeded(): Unit = {
    if (isInitialized) return
    isInitialized = true
    for (pipeline <- _pipelines) {
      pipeline.init(this)
    }
  }

  def cron(specification: String): IObservable[RxCronEvent] = {
    requests
      .filter((evt: RxHttpRequestEvent) => evt.request.getRequestURI == RxImpl.getCronUrl(specification))
      .transform((evt: RxHttpRequestEvent) => new RxCronEvent)
  }

  def injector() = _injector

  def uploads: IObservable[RxUploadEvent] = {
    import scala.collection.JavaConverters._

    requests
      .filter((evt: RxHttpRequestEvent) => evt.request.getRequestURI == RxImpl.getUploadsUrl)
      .transform((event: RxHttpRequestEvent) => {
      val blobInfos: Map[String, Set[BlobInfo]] = blobstoreService.getBlobInfos(event.request).asScala.mapValues((list: util.List[BlobInfo]) => list.asScala.toSet).toMap
      new RxUploadEvent(this, event, blobInfos)
    })
  }

  def taskqueue[T <: java.io.Serializable](queueName: String): IObserver[RxTask[T]] = {
    Observers.asObserver((value: RxTask[T]) => RxTasks.enqueue(queueName, value))
  }

  def tasks[T <: java.io.Serializable](queueName: String, payloadClass: Class[T]): IObservable[RxTask[T]] = {
    tasks(queueName, TypeToken.of(payloadClass))
  }

  def tasks[T <: java.io.Serializable](queueName: String, typeToken: TypeToken[T]): IObservable[RxTask[T]] = {
    requests.filter((event: RxHttpRequestEvent) => {
      val request: HttpServletRequest = event.request
      val requestQueueName: String = request.getHeader("X-AppEngine-QueueName")
      Objects.equals(requestQueueName, queueName)
    })
      .transform((event: RxHttpRequestEvent) => {
      try {
        event.sendOk()
        RxTask.fromRequest(event.request)
      }
      catch {
        case e: IOException => {
          throw new RuntimeException(e)
        }
      }
    })
  }

  def updates: IObservable[RxVersionUpdateEvent] = {
    initialized().transform(new DoFn[RxInitializationEvent, RxVersionUpdateEvent] {
      def process(rxInitializationEvent: RxInitializationEvent, emitFn: (RxVersionUpdateEvent) => Unit): Unit = {
        RxImpl.log.info("Checking version...")
        RxImpl.log.info(System.getProperties.toString)
      }
    })
  }

  def initialized(): IObservable[RxInitializationEvent] = initializationStream

  def requests: IObservable[RxHttpRequestEvent] = requestsStream

  def handleRequest(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    initIfNeeded()
    RxImpl.log.fine(s"handleRequest(${request.getRequestURI})")
    val rxResponse: RxHttpResponse = new RxHttpResponse(response)
    requestsStream.onNext(new RxHttpRequestEvent(this, request, rxResponse))
    if (!rxResponse.hasResponse) {
      rxResponse.sendError(404, request.getRequestURI)
      RxImpl.log.fine("Error 404 : request not processed")
    }
  }

  def onContextInitialized(): Unit = {
    initIfNeeded()
    RxImpl.log.fine("onContextInitialized")
    initializationStream.onNext(new RxInitializationEvent)
  }

  private var isInitialized: Boolean = false
  private val requestsStream: RxPushStream[RxHttpRequestEvent] = new RxPushStream[RxHttpRequestEvent](injector())
  private val initializationStream: RxPushStream[RxInitializationEvent] = new RxPushStream[RxInitializationEvent](injector())
}