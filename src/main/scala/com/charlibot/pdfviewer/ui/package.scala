package com.charlibot.pdfviewer

import zio._

// rename to viewer
package object ui {

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
