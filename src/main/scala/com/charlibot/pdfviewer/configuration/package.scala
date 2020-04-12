package com.charlibot.pdfviewer

import pureconfig.ConfigSource
import zio.{Has, Layer, Task, ZIO, ZLayer}

package object configuration {

  type Configuration = Has[ApiConfig] with Has[PdfsConfig]

  case class AppConfig(api: ApiConfig, pdfs: PdfsConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class PdfsConfig(basepath: String)

  val apiConfig: ZIO[Has[ApiConfig], Throwable, ApiConfig] = ZIO.access(_.get)
  val pdfsConfig: ZIO[Has[PdfsConfig], Throwable, PdfsConfig] = ZIO.access(_.get)

  object Configuration {
    import pureconfig.generic.auto._
    val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.api) ++ Has(c.pdfs)))
  }

}
