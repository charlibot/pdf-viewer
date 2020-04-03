package com.charlibot.pdfviewer

import com.charlibot.pdfviewer.fx.Ui
import scalafx.application.JFXApp
import zio.{BootstrapRuntime, IO, ZEnv, ZIO}

// starts the fx application alongside the HTTP server
object Main extends JFXApp with BootstrapRuntime {

  stage = Ui.stage

  /**
   * The main function of the application, which will be passed the command-line
   * arguments to the program and has to return an `IO` with the errors fully handled.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = HttpServer.run(args)

  override def main(args0: Array[String]): Unit =
    try sys.exit(
      unsafeRun(
        (for {
          fxFiber <- ZIO.effect(super.main(args0)).fork
          fiber <- run(args0.toList).fork
          zipped = fxFiber *> fiber
          _ <- IO.effectTotal(java.lang.Runtime.getRuntime.addShutdownHook(new Thread {
            override def run() = {
              val _ = unsafeRunSync(zipped.interrupt)
            }
          }))
          // TODO: Want to stop everything if fxFiber is stopped.
          result <- zipped.join
          _      <- zipped.interrupt
        } yield result)
      )
    )
    catch { case _: SecurityException => }

}
