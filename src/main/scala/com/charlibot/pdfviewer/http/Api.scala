package com.charlibot.pdfviewer.http

import com.charlibot.pdfviewer.pdfs._
import com.charlibot.pdfviewer.ui._
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, UrlForm}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._
import io.circe.generic.auto._

final case class Api[R <: Ui with Pdfs](queueViewerOps: fs2.concurrent.Queue[Task, ViewerOps]) {

  type ApiTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[ApiTask, A] = jsonOf[ApiTask, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[ApiTask, A] = jsonEncoderOf[ApiTask, A]

  val dsl: Http4sDsl[ApiTask] = Http4sDsl[ApiTask]
  import dsl._

  def route: HttpRoutes[ApiTask] = {
    HttpRoutes.of[ApiTask] {
      case POST -> Root / "viewer" / "next" =>
        for {
          _ <- queueViewerOps.enqueue1(Next())
          response <- Ok("Next page")
        } yield response

      case GET -> Root / "pdfs" =>
        listPdfs.foldM(t => InternalServerError(t.getMessage), pdfs => Ok(pdfs))

      case req @ POST -> Root / "viewer"/ "load" =>
        req
          .decode[UrlForm] { data =>
            data.values.get("pdf").flatMap(_.uncons) match {
              case Some((pdfUrl, _)) =>
                for {
                  _ <- queueViewerOps.enqueue1(Load(s"/pdfs/$pdfUrl"))
                  response <- Ok("")
                } yield response
              case None =>
                BadRequest(s"Invalid data: " + data)
            }
          }
    }
  }

}
