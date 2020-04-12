package com.charlibot.pdfviewer

import cats.effect.ExitCode
import com.charlibot.pdfviewer.configuration.Configuration
import com.charlibot.pdfviewer.http.{Api, Views}
import com.charlibot.pdfviewer.pdfs.Pdfs
import com.charlibot.pdfviewer.ui.Ui
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

object Main extends App {

  type AppEnvironment = Configuration with Ui with Clock with Blocking with Pdfs

  type AppTask[A] = RIO[AppEnvironment, A]

  val pdfs = Configuration.live >>> Pdfs.live

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] = for {
      api <- configuration.apiConfig
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
      httpApp = Router[AppTask](
        "/api" -> Api().route,
        "/" -> Views(blockingEC).route
      ).orNotFound

      server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        BlazeServerBuilder[AppTask]
          .bindHttp(api.port, api.endpoint)
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[AppTask, AppTask, ExitCode]
          .drain
      }
    } yield server

    program.provideSomeLayer[ZEnv](Configuration.live ++ pdfs ++ Ui.live)foldM(
      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
      _ => IO.succeed(0)
    )
  }
}