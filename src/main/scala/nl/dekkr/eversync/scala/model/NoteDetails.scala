package nl.dekkr.eversync.scala.model

case class NoteDetails(filedetails: FileDetails, title : String, notebook: Option[String] = None,  tags: List[String]= List("EverSync"))