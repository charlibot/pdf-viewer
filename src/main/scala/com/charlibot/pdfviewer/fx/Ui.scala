package com.charlibot.pdfviewer.fx

import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color.{DarkGray, DarkRed, Red, White}
import scalafx.scene.paint.{Color, LinearGradient, Stops}
import scalafx.scene.text.Text

object Ui {

  val textProperty = StringProperty("Scala")
  val changeScreen = BooleanProperty(false)

  val stage = new PrimaryStage {
    //        initStyle(StageStyle.Unified)
    title = "ScalaFX Hello World"
    scene = new Scene {
      fill = Color.rgb(38, 38, 38)
      content = new HBox {
        padding = Insets(50, 80, 50, 80)
        children = Seq(
          new Text {
            text <== textProperty
            style = "-fx-font: normal bold 100pt sans-serif"
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(Red, DarkRed))
          },
          new Text {
            text = "FX"
            style = "-fx-font: italic bold 100pt sans-serif"
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(White, DarkGray)
            )
            effect = new DropShadow {
              color = DarkGray
              radius = 15
              spread = 0.25
            }
          }
        )
      }
    }

  }

}
