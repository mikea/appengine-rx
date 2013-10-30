package com.mikea.gae.rx

import com.google.appengine.api.blobstore.BlobstoreService
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
import com.mikea.gae.rx.base.{DoFn, Observers, IObserver}

@Singleton object RxImpl {
  private[rx] def getCronUrl(cronSpecification: String): String = RxModule.RX_CRON_URL_BASE + cronSpecification.replaceAll(" ", "_")

  private[rx] def getUploadsUrl: String = RxModule.RX_UPLOADS_BASE

  private final val log: Logger = Loggers.getContextLogger
}

@Singleton class RxImpl @Inject() (pipelines: Set[RxPipeline], injector: Injector, blobstoreService: BlobstoreService) extends Rx {

  private def initIfNeeded(): Unit = {
    if (isInitialized) return
    isInitialized = true
    for (pipeline <- pipelines) {
      pipeline.init(this)
    }
  }

  def cron(specification: String): RxStream[RxCronEvent] = {
    requests
      .filter((evt: RxHttpRequestEvent) => evt.request.getRequestURI == RxImpl.getCronUrl(specification))
      .transform((evt: RxHttpRequestEvent) => new RxCronEvent)
  }

  def getInjector: Injector = injector

  def uploads: RxStream[RxUploadEvent] = {
    requests
      .filter((evt: RxHttpRequestEvent) => evt.request.getRequestURI == RxImpl.getUploadsUrl)
      .transform((event: RxHttpRequestEvent) =>new RxUploadEvent(event, blobstoreService.getBlobInfos(event.request)))
  }

  def taskqueue[T <: java.io.Serializable](queueName: String): IObserver[RxTask[T]] = {
    Observers.asObserver((value: RxTask[T]) => RxTasks.enqueue(queueName, value))
  }

  def tasks[T <: java.io.Serializable](queueName: String, payloadClass: Class[T]): RxStream[RxTask[T]] = {
    tasks(queueName, TypeToken.of(payloadClass))
  }

  def tasks[T <: java.io.Serializable](queueName: String, typeToken: TypeToken[T]): RxStream[RxTask[T]] = {
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

  def updates: RxStream[RxVersionUpdateEvent] = {
    initialized().transform(new DoFn[RxInitializationEvent, RxVersionUpdateEvent] {
      def process(rxInitializationEvent: RxInitializationEvent, emitFn: (RxVersionUpdateEvent) => Unit): Unit = {
        RxImpl.log.info("Checking version...")
        RxImpl.log.info(System.getProperties.toString)
      }
    })
  }

  def initialized(): RxStream[RxInitializationEvent] = initializationStream

  def requests: RxStream[RxHttpRequestEvent] = requestsStream

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
  private val requestsStream: RxPushStream[RxHttpRequestEvent] = new RxPushStream[RxHttpRequestEvent](this)
  private val initializationStream: RxPushStream[RxInitializationEvent] = new RxPushStream[RxInitializationEvent](this)
}