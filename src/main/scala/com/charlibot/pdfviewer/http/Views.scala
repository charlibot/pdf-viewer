package com.charlibot.pdfviewer.http

import com.charlibot.pdfviewer.pdfs.listPdfs
import cats.effect.Blocker
import cats.syntax.semigroupk._
import org.http4s.implicits._
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import zio.{RIO, Task}
import zio.interop.catz._
import org.http4s.server.staticcontent._
import zio.blocking.Blocking

import scala.concurrent.ExecutionContext

final case class Views[R](blockingEC: ExecutionContext) {

  type ViewsTask[A] = RIO[R, A]

  val dsl: Http4sDsl[ViewsTask] = Http4sDsl[ViewsTask]
  import dsl._

  def index: HttpRoutes[ViewsTask] = {
    HttpRoutes.of[ViewsTask] {
      case request @ GET -> Root =>
        StaticFile.fromResource("index.html", Blocker.liftExecutionContext(blockingEC), Some(request))
          .getOrElseF(NotFound())
    }
  }

  val assets = resourceService[ViewsTask](ResourceService.Config("assets", Blocker.liftExecutionContext(blockingEC)))

  val route = index <+> assets

}
