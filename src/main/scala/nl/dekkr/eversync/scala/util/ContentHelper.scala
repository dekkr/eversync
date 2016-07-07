package nl.dekkr.eversync.scala.util

import java.io.File

object ContentHelper {


  def fillNoteTemplate(content: String) = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
    "<en-note>" +
    content +
    "</en-note>"

  def fileNameToTitle(file: File) : String = {
    val fileName = file.getName.replace("_"," ")
    try {
      fileName.substring(0, fileName.lastIndexOf("."))
    } catch {
      case e : Exception => fileName
    }
  }


}
