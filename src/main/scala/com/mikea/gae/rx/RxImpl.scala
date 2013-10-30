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
import com.mikea.gae.rx.base.{IObservable, DoFn, IObserver}
import com.google.appengine.api.memcache.{Expiration, MemcacheService}
import com.mikea.gae.GaeUtil
import com.googlecode.objectify.ObjectifyService._
import com.googlecode.objectify.Key
import com.mikea.gae.rx.model.AppVersion
import java.util
import scala.collection.mutable

@Singleton object RxImpl {
  private[rx] def getCronUrl(cronSpecification: String): String = s"${RxUrls.RX_CRON_URL_BASE}${cronSpecification.replaceAll(" ", "_")}"

  private[rx] def getUploadsUrl: String = RxUrls.RX_UPLOADS_BASE

  private final val log: Logger = Loggers.getContextLogger
}

@Singleton class RxImpl @Inject()(
    _pipelines: java.util.Set[RxPipeline],
    _injector: Injector, 
    _blobstoreService: BlobstoreService,
    _memcache : MemcacheService) extends Rx {

  private def initIfNeeded(): Unit = {
    if (isInitialized) return
    isInitialized = true

    import scala.collection.JavaConverters._
    for (pipeline <- _pipelines.asScala) {
      pipeline.init(this)
    }
  }

  def cron(specification: String): IObservable[RxCronEvent] = {
    requests
      .filter((evt: RxHttpRequestEvent) => evt.request.getRequestURI == RxImpl.getCronUrl(specification))
      .map((evt: RxHttpRequestEvent) => new RxCronEvent)
  }

  def injector() = _injector

  def upload(): IObservable[RxUploadEvent] = {
    requests
      .filter((evt: RxHttpRequestEvent) => evt.request.getRequestURI == RxImpl.getUploadsUrl)
      .map((event: RxHttpRequestEvent) => {
      val javaMap: util.Map[String, util.List[BlobInfo]] = _blobstoreService.getBlobInfos(event.request)

      import scala.collection.JavaConverters._
      val scalaMap: mutable.Map[String, util.List[BlobInfo]] = javaMap.asScala
      val blobInfos: Map[String, Set[BlobInfo]] = scalaMap.mapValues((list: java.util.List[BlobInfo]) => list.asScala.toSet).toMap
          new RxUploadEvent(this, event, blobInfos)
    })
  }

  def taskqueue[T <: java.io.Serializable](queueName: String): IObserver[RxTask[T]] = {
    IObserver.asObserver((value: RxTask[T]) => RxTasks.enqueue(queueName, value))
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
      .map((event: RxHttpRequestEvent) => {
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

  def appVersionUpdate(): IObservable[RxVersionUpdateEvent] = {
    contextInitialized().map(new DoFn[RxInitializationEvent, RxVersionUpdateEvent] {
      def process(rxInitializationEvent: RxInitializationEvent, emitFn: (RxVersionUpdateEvent) => Unit): Unit = {
        val applicationVersion: String = GaeUtil.getApplicationVersion
        RxImpl.log.info("Checking version " + applicationVersion)

        val memcacheVersion: AnyRef = _memcache.get(RxMemcacheKeys.CURRENT_VERSION)
        if (memcacheVersion != null && memcacheVersion.toString.equals(applicationVersion)) {
          RxImpl.log.info("Current according to memcache")
          return
        }

        val key: Key[AppVersion] = Key.create(classOf[AppVersion], applicationVersion)
        val dbAppVersion: AppVersion = ofy.load.key(key).now()

        if (dbAppVersion != null) {
          RxImpl.log.info("Current according to db")
          _memcache.put(RxMemcacheKeys.CURRENT_VERSION, applicationVersion, Expiration.byDeltaSeconds(3600 * 24))
          return
        }

        RxImpl.log.info("Version changed")

        // fore event
        emitFn(new RxVersionUpdateEvent(applicationVersion))

        ofy.save().entity(AppVersion.forVersion(applicationVersion)).now()
        _memcache.put(RxMemcacheKeys.CURRENT_VERSION, applicationVersion, Expiration.byDeltaSeconds(3600 * 24))
      }
    })
  }

  def contextInitialized(): IObservable[RxInitializationEvent] = initializationStream

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