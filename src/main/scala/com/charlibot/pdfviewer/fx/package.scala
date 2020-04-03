package com.charlibot.pdfviewer

import zio._

package object fx {

  type Fx = Has[Fx.Service]

  object Fx {
    trait Service {
      def next: Task[Int]
    }

    val live: Layer[Throwable, Fx] = ZLayer.succeed(new Service {
      override def next: Task[Int] = ZIO.effect(Ui.textProperty() = "Jaas").map(_ => 1)
    })
  }

  def next: RIO[Fx, Int] = RIO.accessM(_.get.next)

}
