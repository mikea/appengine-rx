package com.mikea.gae.rx

import com.google.inject.Injector
import javax.inject.Inject
import com.mikea.gae.rx.impl.RxImplConfigGen

/**
 * @author mike.aizatsky@gmail.com
 */
object RxMainImpl {
  def main(injector: Injector, args: Array[String]): Unit = {
    injector.getInstance(classOf[RxMainImpl]).run(args)
  }
}

class RxMainImpl @Inject()(pipelines: java.util.Set[RxPipeline], rx: RxImplConfigGen) {
  private def run(args: Array[String]): Unit = {
    import scala.collection.JavaConversions._
    pipelines.map(_.init())
    rx.generateConfigs()
  }
}