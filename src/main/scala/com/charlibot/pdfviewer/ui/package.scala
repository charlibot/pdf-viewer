package com.charlibot.pdfviewer

import zio._

package object ui {

  type Fx = Has[Fx.Service]

  object Fx {
    trait Service {
      def next: Task[Int]
    }

    val live: Layer[Throwable, Fx] = ZLayer.succeed(new Service {
      override def next: Task[Int] = PdfViewerUi.nextPage
    })
  }

  def next: RIO[Fx, Int] = RIO.accessM(_.get.next)

}
