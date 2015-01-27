package com.mikea.gae.rx.impl

import com.google.appengine.api.blobstore.{BlobInfo, BlobstoreService}
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import com.mikea.util.{ServletStyleUriPatternMatcher, Loggers}
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.logging.Logger
import com.mikea.gae.rx.base._
import com.google.appengine.api.memcache.{Expiration, MemcacheService}
import com.googlecode.objectify.ObjectifyService._
import com.googlecode.objectify.Key
import com.mikea.gae.rx.model.AppVersion
import java.util
import scala.collection.mutable
import javax.servlet.FilterChain
import com.google.appengine.api.utils.SystemProperty
import com.mikea.gae.rx.tasks.TaskQueue
import com.mikea.gae.rx._
import com.mikea.gae.rx.events._
import com.google.appengine.api.taskqueue.TaskOptions

private[rx] object RxImpl {
  private[rx] def getCronUrl(cronSpecification: String): String = s"${RxUrls.RX_CRON_URL_BASE}${cronSpecification.replaceAll(" ", "_")}"
  private[rx] def getUploadsUrl: String = RxUrls.RX_UPLOADS_BASE
}

@Singleton private[rx] class RxImpl @Inject()(
    pipelines: java.util.Set[RxPipeline],
    val injector: Injector,
    blobstoreService: BlobstoreService,
    memcache : MemcacheService) extends Rx {

  private val log: Logger = Loggers.getContextLogger

  private val requestsObservable: PushObservable[RxHttpRequest] = new PushObservable[RxHttpRequest]
  private val requestsObserver: Observer[RxHttpResponse] = new Observer[RxHttpResponse] {
    def onError(e: Exception) = ???  // todo
    def onCompleted() = ???  // todo
    def onNext(response: RxHttpResponse) = response.render()
  }
  private var requestsSlot : TransformerSlot[RxHttpRequest, RxHttpResponse] = Transformer.combine(requestsObserver, requestsObservable)

  private def initIfNeeded(): Unit = {
    if (isInitialized) return
    isInitialized = true

    import scala.collection.JavaConverters._
    pipelines.asScala.map(_.init())
  }

  def cron(specification: String): TransformerSlot[RxCronEvent, RxHttpResponse] = {
    requests(RxImpl.getCronUrl(specification)).mapOut((evt: RxHttpRequest) => new RxCronEvent(evt))
  }

  def upload(): TransformerSlot[RxUploadEvent, RxHttpResponse] = {
    requests(RxImpl.getUploadsUrl)
      .mapOut((event: RxHttpRequest) => {
      val javaMap: util.Map[String, util.List[BlobInfo]] = blobstoreService.getBlobInfos(event.httpRequest)

      import scala.collection.JavaConverters._
      val scalaMap: mutable.Map[String, util.List[BlobInfo]] = javaMap.asScala
      val blobInfos: Map[String, Set[BlobInfo]] = scalaMap.mapValues((list: java.util.List[BlobInfo]) => list.asScala.toSet).toMap
          new RxUploadEvent(event, blobInfos)
    })
  }

  def enqueue(queueName : String): Observer[TaskOptions] = TaskQueue.enqueue(queueName)

  def taskqueue(): TransformerSlot[RxTaskEvent, RxHttpResponse] = {
    requests().filter(TaskQueue.isTaskQueueRequest)
              .mapOut((request: RxHttpRequest) => new RxTaskEvent(request))
  }

  def taskqueue(queueName: String): TransformerSlot[RxTaskEvent, RxHttpResponse] = {
    taskqueue().filter((event: RxTaskEvent) => event.queueName.equals(queueName))
  }

  def appVersionUpdate(): Observable[RxVersionUpdateEvent] = {
    for {
      rxInitializationEvent <- contextInitialized()
      if isNewAppVersion(rxInitializationEvent)
    } yield new RxVersionUpdateEvent(SystemProperty.applicationVersion.get)
  }

  private def isNewAppVersion(rxInitializationEvent: RxInitializationEvent) : Boolean = {
    val applicationVersion: String = SystemProperty.applicationVersion.get
    log.info("Checking version " + applicationVersion)

    val memcacheVersion: AnyRef = memcache.get(RxMemcacheKeys.CURRENT_VERSION)
    if (memcacheVersion != null && memcacheVersion.toString.equals(applicationVersion)) {
      log.info("Current according to memcache")
      return false
    }

    val key: Key[AppVersion] = Key.create(classOf[AppVersion], applicationVersion)
    val dbAppVersion: AppVersion = ofy.load.key(key).now()

    if (dbAppVersion != null) {
      log.info("Current according to db")
      memcache.put(RxMemcacheKeys.CURRENT_VERSION, applicationVersion, Expiration.byDeltaSeconds(3600 * 24))
      return false
    }

    ofy.save().entity(AppVersion.forVersion(applicationVersion)).now()
    memcache.put(RxMemcacheKeys.CURRENT_VERSION, applicationVersion, Expiration.byDeltaSeconds(3600 * 24))

    true
  }

  def contextInitialized(): Observable[RxInitializationEvent] = initializationStream

  def requests() = requestsSlot

  def requests(pattern: String): TransformerSlot[RxHttpRequest, RxHttpResponse] = {
    val matcher: ServletStyleUriPatternMatcher = new ServletStyleUriPatternMatcher(pattern)
    requests().filter((request : RxHttpRequest) => matcher.matches(request.httpRequest.getRequestURI))
  }

  def init(): Unit = {
    initIfNeeded()
    log.fine("init")
    initializationStream.onNext(new RxInitializationEvent)
  }
  
  def destroy(): Unit = { }

  def doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): Unit = {
    val requestUri: String = request.getRequestURI
    initIfNeeded()
    log.fine(s"doFilter($requestUri)")

    val event: RxHttpRequest = new RxHttpRequest(request, response)
    requestsObservable.onNext(event)

    if (!event.processed) {
      log.fine(s"request to $requestUri not processed, falling through.")
      chain.doFilter(request, response)
    }
  }


  private var isInitialized: Boolean = false

  private val initializationStream: PushObservable[RxInitializationEvent] = new PushObservable[RxInitializationEvent]
}