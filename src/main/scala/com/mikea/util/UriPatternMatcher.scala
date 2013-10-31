package com.mikea.util

/**
 * @author mike.aizatsky@gmail.com
 */
trait UriPatternMatcher {
  def matches(uri: String): Boolean

}
