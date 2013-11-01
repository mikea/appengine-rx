appengine-rx - reactive data processing in Scala on Appengine.
============

Application of reactive programming ideas not to request processing, but to data processing pipelines.
Very early stage of development.

Entry points:
   * install RxModule into your Guice configuration module.
   * install any number of RxPipeline interface implementation
   * use incoming Rx module to obtain "root" observables and build pipelines on it.
