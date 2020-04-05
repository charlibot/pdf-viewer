package com.charlibot.pdfviewer.fx

import org.icepdf.core.Memento
import org.icepdf.ri.common.{SwingController, SwingViewBuilder}
import scalafx.embed.swing.SwingNode
import scalafx.scene.layout.BorderPane;

object PdfViewerUi {

  def createViewer(): BorderPane = {
    val swingController = new SwingController()
    swingController.setIsEmbeddedComponent(true)
    swingController.openDocument("/Users/charlievans/Documents/Self-Evaluation Guide.pdf")
    val swingViewBuilder = new SwingViewBuilder(swingController)
    val viewerPanel = swingViewBuilder.buildViewerPanel()
    viewerPanel.revalidate()

    val swingNode = new SwingNode()
    swingNode.setContent(viewerPanel)
    val borderPane = new BorderPane()
    borderPane.center = swingNode

    borderPane.prefHeight = 600
    borderPane.prefWidth = 1025
    viewerPanel.setSize(1025, 600)

    swingController.goToDeltaPage(swingController.getDocumentViewController.getDocumentView.getNextPageIncrement)

    borderPane
  }

}
