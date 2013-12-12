package com.mikea.gae.rx.tasks

import com.mikea.gae.rx.base.{PushObservable, Observer, Observable, Subject}
import java.io.Serializable
import scala.reflect.runtime.universe._
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import java.util.Random
import java.util.logging.Logger
import com.mikea.util.Loggers
import com.twitter.bijection.Bijection

/**
 * @author mike.aizatsky@gmail.com
 */
object RxPeriodicTask {
  private val LOG: Logger = Loggers.getContextLogger

  // todo: this is kind of ugly. make it better.
  def periodicTasks[S, T <: Serializable : TypeTag](taskFactory : RxTasksFactory,
                                                    queueName : String,
                                                    bijection : Bijection[T, S],
                                                    strategy : RescheduleStrategy[S],
                                                    baseNameFn : S => String) : Subject[S] = {
    val taskqueue = taskFactory[RxPeriodicTaskPayload[T]](queueName)
    val taskqueueObserver: Observer[RxTask[RxPeriodicTaskPayload[T]]] = taskqueue
    val taskqueueObservable: Observable[RxTask[RxPeriodicTaskPayload[T]]] = taskqueue

    val observable = new PushObservable[S]

    taskqueueObservable
      .map((task: RxTask[RxPeriodicTaskPayload[T]]) => {
      val s: S = bijection.apply(task.payload.payload)
      observable.onNext(s)
        computeContinuation(task, strategy, s)
      })
      .flatten()
      .sink(taskqueueObserver)

    val observer : Observer[S] = taskqueueObserver.unmap((s : S) => {
      val payload: RxPeriodicTaskPayload[T] = new RxPeriodicTaskPayload[T](bijection.invert(s), baseNameFn(s), 0)
      RxTask(payload = payload, name = Some(payload.name))
    })

    Subject.combine(observable, observer)
  }

  private def computeContinuation[S, T <: Serializable : TypeTag](task: RxTask[RxPeriodicTaskPayload[T]], strategy : RescheduleStrategy[S], s : S) : Option[RxTask[RxPeriodicTaskPayload[T]]] = {
    val countdown = strategy.countdown(s)
    countdown.map((d: Duration) => computeContinuation(task, d))
  }

  private def computeContinuation[T <: Serializable : TypeTag](task: RxTask[RxPeriodicTaskPayload[T]], duration : Duration) : RxTask[RxPeriodicTaskPayload[T]] = {
    val newPayload: RxPeriodicTaskPayload[T] = new RxPeriodicTaskPayload[T](task.payload.payload, task.payload.baseName, task.payload.generation + 1)

    val countdownSec = duration.toUnit(TimeUnit.SECONDS) * (1.0 + new Random().nextFloat() / 10.0) // up to 10% of randomness

    task.copy(payload = newPayload,
              countdown = Some(Duration.create(countdownSec, TimeUnit.SECONDS)),
              name = Some(newPayload.name))
  }
}

// todo: payload & basename do not change through executions. extract to a separate class?
private class RxPeriodicTaskPayload[T <: Serializable : TypeTag](val payload: T, val baseName: String, val generation: Int) extends Serializable {
  val name = baseName + "---rx---" + generation
}

trait RescheduleStrategy[T] {
  def countdown(t : T) : Option[Duration]
}