package com.charlibot.pdfviewer

// rename to viewer
package object ui {

  sealed trait ViewerOps
  case class Load(pdfUrl: String) extends ViewerOps
  case class Next() extends ViewerOps
  case class Prev() extends ViewerOps

  sealed trait FromClient
  case class Loaded(pdfUrl: String) extends FromClient

}
