package com.charlibot.pdfviewer.ui

import javax.swing.{JFrame, WindowConstants}

import scala.swing.{Component, FlowPanel, Frame, MainFrame}

object Ui {

  val frame: JFrame = {
    val viewer = PdfViewerUi.createViewer()
    val applicationFrame = new JFrame
    applicationFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    applicationFrame.getContentPane.add(viewer)

    applicationFrame
  }

}
