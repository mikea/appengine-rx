package com.mikea.gae.rx

import com.google.inject.Injector
import javax.inject.Inject
import java.util.Set

/**
 * @author mike.aizatsky@gmail.com
 */
object RxMainImpl {
  def main(injector: Injector, args: Array[String]) {
    injector.getInstance(classOf[RxMainImpl]).run(args)
  }
}

class RxMainImpl @Inject()(_pipelines: java.util.Set[RxPipeline], _rx: RxImplConfigGen) {
  private def run(args: Array[String]) {
    import scala.collection.JavaConversions._
    for (pipeline <- _pipelines) {
      pipeline.init(_rx)
    }
    _rx.generateConfigs()
  }
}