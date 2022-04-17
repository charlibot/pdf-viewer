package com.charlibot.pdfviewer.http

import cats.effect.Blocker
import cats.syntax.semigroupk._
import com.charlibot.pdfviewer.ui.{ViewerOps, FromClient}
import fs2.{Pipe, Stream}
import fs2.concurrent.Queue
import org.http4s.implicits._
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.Http4sDsl
import zio.{RIO, Task}
import zio.interop.catz._
import org.http4s.server.staticcontent._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import io.circe._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.syntax._
import io.circe.parser._

import scala.concurrent.ExecutionContext

final case class Views[R](
    viewerOperations: Queue[Task, ViewerOps],
    queue: Queue[Task, FromClient],
    blockingEC: ExecutionContext,
    pdfsPath: String
) {

  type ViewsTask[A] = RIO[R, A]

  implicit val customConfig: Configuration =
    Configuration.default.withDefaults.withDiscriminator("type").withKebabCaseConstructorNames

  val dsl: Http4sDsl[ViewsTask] = Http4sDsl[ViewsTask]
  import dsl._

  def views: HttpRoutes[ViewsTask] = {
    HttpRoutes.of[ViewsTask] {
      case request @ GET -> Root =>
        StaticFile
          .fromResource("index.html", Blocker.liftExecutionContext(blockingEC), Some(request))
          .getOrElseF(NotFound())
      case request @ GET -> Root / "viewer" =>
        StaticFile
          .fromResource("pdfview.html", Blocker.liftExecutionContext(blockingEC), Some(request))
          .getOrElseF(NotFound())
      case GET -> Root / "viewer-ops" =>
        val toClient: Stream[ViewsTask, WebSocketFrame.Text] =
          viewerOperations.dequeue
            .map(msg => Text(msg.asJson.toString))

        def processInput(wsfStream: Stream[ViewsTask, WebSocketFrame]): Stream[ViewsTask, Unit] = {
          wsfStream
            .collect { case Text(text, _) =>
              decode[FromClient](text)
            }
            .collect { case Right(r) => r }
            .through(
              queue.asInstanceOf[Queue[ViewsTask, FromClient]].enqueue
            ) // TODO: Fix this asInstanceOf - EURGH
        }

        WebSocketBuilder[ViewsTask].build(toClient, processInput)
    }
  }

  val assets = resourceService[ViewsTask](
    ResourceService.Config("assets", Blocker.liftExecutionContext(blockingEC))
  )

  val pdfs = fileService[ViewsTask](
    FileService.Config(pdfsPath, Blocker.liftExecutionContext(blockingEC), "/pdfs")
  )

  val route = views <+> assets <+> pdfs

}
