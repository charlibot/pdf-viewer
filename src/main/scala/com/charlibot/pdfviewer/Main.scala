package com.charlibot.pdfviewer

import com.charlibot.pdfviewer.ui.Ui
import zio._
import zio.console.putStrLn

import scala.swing.{Dimension, Swing}

// starts the ui application alongside the HTTP server
object Main extends App {

  def runUi(args: List[String]) = Swing.onEDTWait {
    val t = Ui.frame
    if (t.getSize == new Dimension(0,0)) t.pack()
    t.setVisible(true)
  }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val program = for {
      _ <- ZIO.effect(runUi(args))
      server <- HttpServer.server(args)
    } yield server

    program.foldM(
      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
      _ => IO.succeed(0)
    )
  }
}