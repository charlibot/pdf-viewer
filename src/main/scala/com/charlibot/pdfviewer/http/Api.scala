package com.charlibot.pdfviewer.http

import com.charlibot.pdfviewer.ui._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._

final case class Api[R <: Fx]() {

  type ViewerTask[A] = RIO[R, A]

  val dsl: Http4sDsl[ViewerTask] = Http4sDsl[ViewerTask]
  import dsl._

  def route: HttpRoutes[ViewerTask] = {
    HttpRoutes.of[ViewerTask] {
      case POST -> Root / "next" =>
        next.foldM(t => InternalServerError(t.getMessage), index => Ok(s"Current page: $index"))
    }
  }

}
