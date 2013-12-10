package com.mikea.gae.rx.tasks

import java.io.Serializable
import scala.reflect.runtime.universe._
import com.mikea.gae.rx.base.Subject

/**
 * @author mike.aizatsky@gmail.com
 */
// todo: the existence of the factory doesn't seem to be like a good thing
trait RxTasksFactory {
  def apply[T <: Serializable : TypeTag](queueName : String): Subject[RxTask[T]]
}
