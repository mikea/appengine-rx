package com.mikea.gae.rx.impl

import com.google.appengine.api.blobstore.{BlobInfo, BlobstoreService}
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import com.mikea.util.{UriPatternMatcher, ServletStyleUriPatternMatcher, Loggers}
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
import scala.collection.immutable.HashMap
import com.google.appengine.api.utils.SystemProperty
import com.mikea.gae.rx.tasks.RxTasks
import com.mikea.gae.rx._
import com.mikea.gae.rx.events._
import com.google.appengine.api.taskqueue.TaskOptions

private[rx] object RxImpl {
  private[rx] def getCronUrl(cronSpecification: String): String = s"${RxUrls.RX_CRON_URL_BASE}${cronSpecification.replaceAll(" ", "_")}"

  private[rx] def getUploadsUrl: String = RxUrls.RX_UPLOADS_BASE

  private final val log: Logger = Loggers.getContextLogger
}

@Singleton private[rx] class RxImpl @Inject()(
    pipelines: java.util.Set[RxPipeline],
    val injector: Injector,
    blobstoreService: BlobstoreService,
    memcache : MemcacheService) extends Rx {

  private def initIfNeeded(): Unit = {
    if (isInitialized) return
    isInitialized = true

    import scala.collection.JavaConverters._
    pipelines.asScala.map(_.init())
  }

  def cron(specification: String): TransformerSlot[RxCronEvent, RxHttpResponse] = {
    requests(RxImpl.getCronUrl(specification)).mapOut((evt: RxHttpRequest) => new RxCronEvent(evt.rx, evt.httpRequest, evt.httpResponse))
  }

  def upload(): TransformerSlot[RxUploadEvent, RxHttpResponse] = {
    requests(RxImpl.getUploadsUrl)
      .mapOut((event: RxHttpRequest) => {
      val javaMap: util.Map[String, util.List[BlobInfo]] = blobstoreService.getBlobInfos(event.httpRequest)

      import scala.collection.JavaConverters._
      val scalaMap: mutable.Map[String, util.List[BlobInfo]] = javaMap.asScala
      val blobInfos: Map[String, Set[BlobInfo]] = scalaMap.mapValues((list: java.util.List[BlobInfo]) => list.asScala.toSet).toMap
          new RxUploadEvent(this, event, blobInfos)
    })
  }

  def taskqueue(queueName: String): Transformer[TaskOptions, RxHttpRequest] = {
    initIfNeeded()

    val observableOption: Option[PushObservable[RxHttpRequest]] = tasksStreams.get(queueName)
    val observable = observableOption.getOrElse(new PushObservable[RxHttpRequest])

    if (!observableOption.isDefined) {
      tasksStreams = tasksStreams + (queueName -> observable)
    }

    Transformer.combine(RxTasks.taskqueue(queueName), observable)
  }

  def appVersionUpdate(): Observable[RxVersionUpdateEvent] = {
    for {
      rxInitializationEvent <- contextInitialized()
      if isNewAppVersion(rxInitializationEvent)
    } yield new RxVersionUpdateEvent(SystemProperty.applicationVersion.get)
  }

  private def isNewAppVersion(rxInitializationEvent: RxInitializationEvent) : Boolean = {
    val applicationVersion: String = SystemProperty.applicationVersion.get
    RxImpl.log.info("Checking version " + applicationVersion)

    val memcacheVersion: AnyRef = memcache.get(RxMemcacheKeys.CURRENT_VERSION)
    if (memcacheVersion != null && memcacheVersion.toString.equals(applicationVersion)) {
      RxImpl.log.info("Current according to memcache")
      return false
    }

    val key: Key[AppVersion] = Key.create(classOf[AppVersion], applicationVersion)
    val dbAppVersion: AppVersion = ofy.load.key(key).now()

    if (dbAppVersion != null) {
      RxImpl.log.info("Current according to db")
      memcache.put(RxMemcacheKeys.CURRENT_VERSION, applicationVersion, Expiration.byDeltaSeconds(3600 * 24))
      return false
    }

    ofy.save().entity(AppVersion.forVersion(applicationVersion)).now()
    memcache.put(RxMemcacheKeys.CURRENT_VERSION, applicationVersion, Expiration.byDeltaSeconds(3600 * 24))

    true
  }

  def contextInitialized(): Observable[RxInitializationEvent] = initializationStream

  def requests(pattern: String): TransformerSlot[RxHttpRequest, RxHttpResponse] = {
    initIfNeeded()

    val observable: PushObservable[RxHttpRequest] = requestObservables.getOrElse(pattern, new PushObservable[RxHttpRequest])
    requestObservables = requestObservables + (pattern -> observable)
    requestMatchers = requestMatchers :+ (new ServletStyleUriPatternMatcher(pattern) -> observable)

    val observer: Observer[RxHttpResponse] = responseObservers.getOrElse(pattern, new Observer[RxHttpResponse] {
      def onError(e: Exception) = ???
      def onCompleted() = ???
      def onNext(response: RxHttpResponse) = response.render()
    })

    Transformer.combine(observer, observable)
  }

  def init(): Unit = {
    initIfNeeded()
    RxImpl.log.fine("init")
    initializationStream.onNext(new RxInitializationEvent)
  }
  
  def destroy(): Unit = { }

  def doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): Unit = {
    val requestUri: String = request.getRequestURI
    initIfNeeded()
    RxImpl.log.fine(s"doFilter($requestUri)")

    val queueName: String = request.getHeader("X-AppEngine-QueueName")
    val tasksStream: Option[PushObservable[RxHttpRequest]] = tasksStreams.get(queueName)
    
    if (tasksStream.isDefined) {
      tasksStream.get.onNext(newRxHttpRequestEvent(request, response))
      return
    }

    for (pair <- requestMatchers) {
      val matcher: UriPatternMatcher = pair._1
      val observable: PushObservable[RxHttpRequest] = pair._2

      if (matcher.matches(requestUri)) {
        RxImpl.log.fine(s"Match found: $matcher $observable")
        observable.onNext(newRxHttpRequestEvent(request, response))
        return
      }
    }

    RxImpl.log.fine(s"filter for $requestUri found. matchers=$requestMatchers")

    chain.doFilter(request, response)
  }


  def newRxHttpRequestEvent(request: HttpServletRequest, response: HttpServletResponse): RxHttpRequest = {
    new RxHttpRequest(this, request, response)
  }

  private var isInitialized: Boolean = false

  private var requestObservables: Map[String, PushObservable[RxHttpRequest]] = HashMap()
  private var responseObservers: Map[String, Observer[RxHttpResponse]] = HashMap()

  private var requestMatchers: List[(UriPatternMatcher, PushObservable[RxHttpRequest])] = List()

  // queue name => stream
  private var tasksStreams: Map[String, PushObservable[RxHttpRequest]] = HashMap()

  private val initializationStream: PushObservable[RxInitializationEvent] = new PushObservable[RxInitializationEvent]
}