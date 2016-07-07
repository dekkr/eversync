package nl.dekkr.eversync.scala

import com.evernote.edam.error.{EDAMErrorCode, EDAMSystemException, EDAMUserException}
import com.evernote.thrift.transport.TTransportException
import nl.dekkr.eversync.scala.model.{FileDetails, NoteDetails}
import nl.dekkr.eversync.scala.service.Evernote
import nl.dekkr.eversync.scala.util.{Config, ContentHelper, FileHelper, Mimetype}

import scala.util.{Failure, Success}


object EverSync extends App {

  var token: String = Config.auth_token
  if (token == "dummy") {
    System.err.println("Please fill in your developer token")
    System.err.println("To get a developer token, go to https://sandbox.evernote.com/api/DeveloperToken.action")
  }
  val evernote: Evernote = new Evernote(token)
  try {
    println(s"Reading files from ${Config.importDirectory}")
    val files = FileHelper.getFileInFolder(Config.importDirectory)
    files.foreach { file =>
      Mimetype.mimeType(file) match {
        case Success(mimeType) =>
          println("-------------------------------")
          println(s"Name: ${file.getName} - mime-type $mimeType")
          val noteDetails = NoteDetails(FileDetails(file, mimeType), ContentHelper.fileNameToTitle(file), None)
          evernote.createNote(noteDetails)

        case Failure(error) => println(s"Could not determine mime-type of file ${file.getName}")
      }

    }
  }
  catch {
    case e: EDAMUserException => {
      if (e.getErrorCode eq EDAMErrorCode.AUTH_EXPIRED) {
        System.err.println("Your authentication token is expired!")
      }
      else if (e.getErrorCode eq EDAMErrorCode.INVALID_AUTH) {
        System.err.println("Your authentication token is invalid!")
      }
      else if (e.getErrorCode eq EDAMErrorCode.QUOTA_REACHED) {
        System.err.println("Your quota has been reached!")
      }
      else {
        System.err.println("Error: " + e.getErrorCode.toString + " parameter: " + e.getParameter)
      }
    }
    case e: EDAMSystemException => {
      System.err.println("System error: " + e.getErrorCode.toString)
    }
    case t: TTransportException => {
      System.err.println("Networking error: " + t.getMessage)
    }
  }

}

