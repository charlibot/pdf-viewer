package com.charlibot.pdfviewer

import zio._

// rename to viewer
package object ui {

  sealed trait ViewerOps
  case class Load(pdfUrl: String) extends ViewerOps
  case class Next() extends ViewerOps

  sealed trait FromClient
  case class Loaded(pdfUrl: String) extends FromClient


  type Ui = Has[Ui.Service]

  object Ui {
    trait Service {
      def next: Task[Int]
    }

    val live: Layer[Throwable, Ui] = ZLayer.succeed(new Service {
      override def next: Task[Int] = ???
    })
  }

  def next: RIO[Ui, Int] = RIO.accessM(_.get.next)

}
