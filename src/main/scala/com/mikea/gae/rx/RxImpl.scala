package com.mikea.gae.rx

import com.google.appengine.api.blobstore.{BlobInfo, BlobstoreService}
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import com.mikea.util.{UriPatternMatcher, ServletStyleUriPatternMatcher, Loggers}
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.Serializable
import java.util.logging.Logger
import com.mikea.gae.rx.base._
import com.google.appengine.api.memcache.{Expiration, MemcacheService}
import com.mikea.gae.GaeUtil
import com.googlecode.objectify.ObjectifyService._
import com.googlecode.objectify.Key
import com.mikea.gae.rx.model.AppVersion
import java.util
import scala.collection.mutable
import scala.reflect.runtime.universe._
import javax.servlet.FilterChain
import scala.collection.immutable.HashMap

object RxImpl {
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

  def cron(specification: String): Observable[RxCronEvent] = {
    requests(RxImpl.getCronUrl(specification))
      .map((evt: RxHttpRequestEvent) => new RxCronEvent)
  }

  def injector() = _injector

  def upload(): Observable[RxUploadEvent] = {
    requests(RxImpl.getUploadsUrl)
      .map((event: RxHttpRequestEvent) => {
      val javaMap: util.Map[String, util.List[BlobInfo]] = _blobstoreService.getBlobInfos(event.request)

      import scala.collection.JavaConverters._
      val scalaMap: mutable.Map[String, util.List[BlobInfo]] = javaMap.asScala
      val blobInfos: Map[String, Set[BlobInfo]] = scalaMap.mapValues((list: java.util.List[BlobInfo]) => list.asScala.toSet).toMap
          new RxUploadEvent(this, event, blobInfos)
    })
  }

  def taskqueue[T <: Serializable : TypeTag](queueName: String): Subject[RxTask[T]] = {
    Subject.combine(taskqueueObservable(queueName), taskqueueObserver(queueName))
  }

  private def taskqueueObserver[T <: java.io.Serializable](queueName: String): Observer[RxTask[T]] = {
    RxTasks.taskqueue(queueName).map((task: RxTask[T]) => task.asTaskOptions())
  }

  private def taskqueueObservable[T <: java.io.Serializable : TypeTag](queueName: String): Observable[RxTask[T]] = {
    taskqueueObservableImpl(queueName).map((event: RxHttpRequestEvent) => RxTask.fromRequest(event.request))
  }

  private def taskqueueObservableImpl(queueName: String): Observable[RxHttpRequestEvent] = {
    initIfNeeded()

    val observableOption: Option[PushObservable[RxHttpRequestEvent]] = tasksStreams.get(queueName)
    if (observableOption.isDefined) {
      return observableOption.get
    }

    val observable: PushObservable[RxHttpRequestEvent] = newPushObservable
    tasksStreams = tasksStreams + (queueName -> observable)
    observable
  }

  def appVersionUpdate(): Observable[RxVersionUpdateEvent] = {
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

  def contextInitialized(): Observable[RxInitializationEvent] = initializationStream

  def requests(pattern: String): Observable[RxHttpRequestEvent] = {
    initIfNeeded()

    val streamOption: Option[PushObservable[RxHttpRequestEvent]] = requestStreams.get(pattern)
    if (streamOption.isDefined) {
      return streamOption.get
    }

    val observable: PushObservable[RxHttpRequestEvent] = newPushObservable
    requestStreams = requestStreams + (pattern -> observable)
    requestMatchers = requestMatchers :+ (new ServletStyleUriPatternMatcher(pattern) -> observable)
    observable
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
    val tasksStream: Option[PushObservable[RxHttpRequestEvent]] = tasksStreams.get(queueName)
    
    if (tasksStream.isDefined) {
      tasksStream.get.onNext(newRxHttpRequestEvent(request, response))
      return
    }

    for (pair <- requestMatchers) {
      val matcher: UriPatternMatcher = pair._1
      val observable: PushObservable[RxHttpRequestEvent] = pair._2

      if (matcher.matches(requestUri)) {
        observable.onNext(newRxHttpRequestEvent(request, response))
        return
      }
    }

    chain.doFilter(request, response)
  }


  def newRxHttpRequestEvent(request: HttpServletRequest, response: HttpServletResponse): RxHttpRequestEvent = {
    new RxHttpRequestEvent(this, request, new RxHttpResponse(response))
  }

  def newPushObservable[T]:PushObservable[T] = new PushObservable[T] {
    def instantiate[C](aClass: Class[C]) = _injector.getInstance(aClass)
  }

  private var isInitialized: Boolean = false

  private var requestStreams: Map[String, PushObservable[RxHttpRequestEvent]] = HashMap()
  private var requestMatchers: List[(UriPatternMatcher, PushObservable[RxHttpRequestEvent])] = List()

  // queue name => stream
  private var tasksStreams: Map[String, PushObservable[RxHttpRequestEvent]] = HashMap()

  private val initializationStream: PushObservable[RxInitializationEvent] = newPushObservable
}