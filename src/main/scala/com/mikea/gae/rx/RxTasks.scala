package com.mikea.gae.rx

import com.google.appengine.api.taskqueue.Queue
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions
import java.io.Serializable

/**
 * @author mike.aizatsky@gmail.com
 */
object RxTasks {
  def enqueue[T <: Serializable](queueName: String, value: RxTask[T]): Unit = {
    val queue: Queue = QueueFactory.getQueue(queueName)
    val taskOptions: TaskOptions = TaskOptions.Builder.withUrl(RxUrls.RX_TASKS_URL_BASE).payload(value.toPayLoad)
    queue.add(taskOptions)
  }
}