package com.mikea.gae.rx.events

import com.mikea.gae.rx.tasks.TaskQueue


/**
 * @author mike.aizatsky@gmail.com
 */
class RxTaskEvent(httpRequest : RxHttpRequest) extends RxHttpRequest(httpRequest) {
  val queueName : String = TaskQueue.queueName(httpRequest).get
}
