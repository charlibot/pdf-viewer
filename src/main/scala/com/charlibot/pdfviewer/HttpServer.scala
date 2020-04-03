package com.charlibot.pdfviewer

import cats.effect.ExitCode
import com.charlibot.pdfviewer.fx.Fx
import com.charlibot.pdfviewer.http.Api
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

object HttpServer {

  type AppEnvironment = Fx with Clock

  type AppTask[A] = RIO[AppEnvironment, A]

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        _ <- ZIO.effect("null")
        httpApp = Router[AppTask](
          "/viewer" -> Api().route
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          BlazeServerBuilder[AppTask]
            .bindHttp(8080, "0.0.0.0")
            .withHttpApp(CORS(httpApp))
            .serve
            .compile[AppTask, AppTask, ExitCode]
            .drain
        }
      } yield server

    program.provideSomeLayer[ZEnv](Fx.live)foldM(
      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
      _ => IO.succeed(0)
    )
  }
}
