package com.charlibot.pdfviewer

import java.io.File
import java.nio.file.{FileSystems, Files}

import com.charlibot.pdfviewer.configuration.{PdfsConfig, pdfsConfig}
import zio.{Has, Task, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

package object pdfs {

  case class Pdf(name: String, path: String, tags: Map[String, String])

  type Pdfs = Has[Pdfs.Service]

  object Pdfs {
    trait Service {
      def listPdfs: List[Pdf]
    }

    private def loadPdfs(basepath: String): Task[List[Pdf]] =
      for {
        dir <- ZIO.effect(FileSystems.getDefault.getPath(basepath))
        pdfFiles <- ZIO.effect(Files.walk(dir).iterator().asScala.filter(Files.isRegularFile(_)).map(_.toFile).filter(_.getName.endsWith(".pdf")))
        pdfs = pdfFiles.map(file => Pdf(file.getName, file.toString, Map())).toList
      } yield pdfs

    val live: ZLayer[Has[PdfsConfig], Throwable, Has[Pdfs.Service]] =
      ZLayer.fromEffect(
        for {
          pdfsConf <- pdfsConfig
          pdfs <- loadPdfs(pdfsConf.basepath)
        } yield new Service {
          override def listPdfs: List[Pdf] = pdfs
        }
      )
  }

  val listPdfs: ZIO[Has[Pdfs.Service], Throwable, List[Pdf]] = ZIO.access(_.get.listPdfs)

}
