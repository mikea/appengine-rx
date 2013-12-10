package com.mikea.gae.rx.impl

/**
 * @author mike.aizatsky@gmail.com
 */
object RxMemcacheKeys {
  final val MEMCACHE_KEY_VERSION = 1
  final val MEMCACHE_KEY_PREFIX = s"$MEMCACHE_KEY_VERSION/_rx"
  final val CURRENT_VERSION = s"$MEMCACHE_KEY_PREFIX/app_version"
}
