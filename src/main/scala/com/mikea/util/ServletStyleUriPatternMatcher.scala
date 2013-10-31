package com.mikea.util

import com.google.inject.servlet.UriPatternType
import com.mikea.util.ServletStyleUriPatternMatcher.Kind
import com.mikea.util.ServletStyleUriPatternMatcher.Kind.Kind

object ServletStyleUriPatternMatcher {
  object Kind extends Enumeration {
    type Kind = Value
    val PREFIX, SUFFIX, LITERAL = Value
  }
}

class ServletStyleUriPatternMatcher(_pattern: String) extends UriPatternMatcher {

  private var pattern: String = null
  private var patternKind: Kind = Kind.LITERAL

  init()


  def init() = {
    if (_pattern.startsWith("*")) {
      this.pattern = _pattern.substring(1)
      this.patternKind = Kind.PREFIX
    }
    else if (_pattern.endsWith("*")) {
      this.pattern = _pattern.substring(0, pattern.length - 1)
      this.patternKind = Kind.SUFFIX
    }
    else {
      this.pattern = _pattern
      this.patternKind = Kind.LITERAL
    }
  }

  def matches(uri: String): Boolean = {
    if (null == uri) {
      return false
    }
    if (patternKind == Kind.PREFIX) {
      return uri.endsWith(pattern)
    }
    else if (patternKind == Kind.SUFFIX) {
      return uri.startsWith(pattern)
    }

    pattern == uri
  }

  def extractPath(path: String): String = {
    if (patternKind == Kind.PREFIX) {
      return null
    }
    else if (patternKind == Kind.SUFFIX) {
      var extract: String = pattern
      if (extract.endsWith("/")) {
        extract = extract.substring(0, extract.length - 1)
      }
      return extract
    }

    path
  }

  def getPatternType = UriPatternType.SERVLET

}