package nl.dekkr.eversync.scala


import com.evernote.edam.error.{EDAMErrorCode, EDAMSystemException, EDAMUserException}
import com.evernote.thrift.transport.TTransportException
import nl.dekkr.eversync.scala.model.{FileDetails, NoteDetails}
import nl.dekkr.eversync.scala.service.Evernote
import nl.dekkr.eversync.scala.util.{Config, ContentHelper, FileHelper, Mimetype}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


object EverSync extends App {

  val log = LoggerFactory.getLogger("EverSync")

  if (Config.archiveAfterUpload) {
    if (!FileHelper.makeDir(Config.archiveDirectory)) {
      log.error("No archive directory available")
      System.exit(1)
    }
  }


  var token: String = Config.auth_token
  if (token == "dummy") {
    log.error("Please fill in your developer token")
    log.error("To get a developer token, go to https://sandbox.evernote.com/api/DeveloperToken.action")
  } else {
    val evernote: Evernote = new Evernote(token)
    try {
      log.info(s"Reading files from ${Config.importDirectory}")
      for {
        file <- FileHelper.getFilesInFolder(Config.importDirectory)
        _ = Mimetype.mimeType(file) match {
          case Success(mimeType) =>
            log.debug(s"name: ${file.getName} - mime-type: $mimeType")
            val noteDetails = NoteDetails(FileDetails(file, mimeType), ContentHelper.fileNameToTitle(file), None)
            evernote.createNote(noteDetails)
            if (Config.archiveAfterUpload) FileHelper.moveFile(file, Config.archiveDirectory)

          case Failure(error) =>
            log.error(s"Could not determine mime-type of file ${file.getName}")
        }
      } yield ()
    }
    catch {
      case _: Throwable => errorHandler _
    }
  }

  def errorHandler(e: Throwable): Unit = {
    e match {
      case e: EDAMUserException =>
        if (e.getErrorCode eq EDAMErrorCode.AUTH_EXPIRED) log.error("Your authentication token is expired!")
        else if (e.getErrorCode eq EDAMErrorCode.INVALID_AUTH) log.error("Your authentication token is invalid!")
        else if (e.getErrorCode eq EDAMErrorCode.QUOTA_REACHED) log.error("Your quota has been reached!")
        else log.error("Error: " + e.getErrorCode.toString + " parameter: " + e.getParameter)
      case e: EDAMSystemException => log.error("System error: " + e.getErrorCode.toString)
      case t: TTransportException => log.error("Networking error: " + t.getMessage)
    }
  }


}

