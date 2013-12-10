package com.mikea.gae.rx.impl

/**
 * @author mike.aizatsky@gmail.com
 */
private[rx] object RxUrls {
  final val RX_URL_BASE: String = "/_rx/"
  final val RX_CRON_URL_BASE: String = RX_URL_BASE + "cron/"
  final val RX_UPLOADS_BASE: String = RX_URL_BASE + "upload/"
  final val RX_TASKS_URL_BASE: String = RX_URL_BASE + "task/"
}
