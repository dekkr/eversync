package nl.dekkr.eversync.scala.util

object ContentHelper {


  def fillNoteTemplate(content: String) = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
    "<en-note>" +
    content +
    "</en-note>"

}
