package nl.dekkr.eversync.scala

import com.evernote.edam.error.{EDAMErrorCode, EDAMSystemException, EDAMUserException}
import com.evernote.thrift.transport.TTransportException
import nl.dekkr.eversync.scala.depricated.EvernoteDemo

object EverSync extends App {
  /** *************************************************************************
    * You must change the following values before running this sample code *
    * **************************************************************************/
  private val AUTH_TOKEN: String = "your developer token"

  /**
    * Console entry point.
    */
  @throws(classOf[Exception])
  //  def main(args: String) {
  var token: String = System.getenv("AUTH_TOKEN")
  if (token == null) {
    token = AUTH_TOKEN
  }
  if (token == AUTH_TOKEN) {
    System.err.println("Please fill in your developer token")
    System.err.println("To get a developer token, go to https://sandbox.evernote.com/api/DeveloperToken.action")
  }
  val demo: EvernoteDemo = new EvernoteDemo(token)
  try {
    demo.listNotes()
    demo.createNote()
    demo.searchNotes()
    demo.updateNoteTag()
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
        System.err.println("Your authentication token is invalid!")
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

