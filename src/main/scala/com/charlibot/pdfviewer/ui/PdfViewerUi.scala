package com.charlibot.pdfviewer.ui

import javax.swing.JPanel
import org.icepdf.core.Memento
import org.icepdf.core.pobjects.Page
import org.icepdf.core.pobjects.actions.Action
import org.icepdf.core.pobjects.annotations.Annotation
import org.icepdf.ri.common.views.{AnnotationCallback, AnnotationComponent, PageViewComponent}
import org.icepdf.ri.common.{SwingController, SwingViewBuilder}
import zio.{Task, ZIO}

import scala.swing.Panel;

object PdfViewerUi {
    val swingController = new SwingController()

  def createViewer(): JPanel = {
    swingController.setIsEmbeddedComponent(true)
    swingController.getDocumentViewController.setAnnotationCallback(new AnnotationCallback {
      override def processAnnotationAction(annotation: Annotation, action: Action, x: Int, y: Int): Unit = {}
      override def pageAnnotationsInitialized(page: Page): Unit = {}
      override def newAnnotation(page: PageViewComponent, annotationComponent: AnnotationComponent): Unit = {}
      override def updateAnnotation(annotationComponent: AnnotationComponent): Unit = {}
      override def removeAnnotation(pageComponent: PageViewComponent, annotationComponent: AnnotationComponent): Unit = {}
    })
    swingController.openDocument("/Users/charlievans/Documents/Self-Evaluation Guide.pdf")
    val swingViewBuilder = new SwingViewBuilder(swingController)
    val viewerPanel = swingViewBuilder.buildViewerPanel()
    viewerPanel.revalidate()

    viewerPanel
  }

  def nextPage: Task[Int] =
    ZIO.effect(swingController.goToDeltaPage(swingController.getDocumentViewController.getDocumentView.getNextPageIncrement))
      .map(_ => swingController.getCurrentPageNumber)

}
