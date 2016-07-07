package nl.dekkr.eversync.scala.service

import java.util.{List => JList}
import nl.dekkr.eversync.scala.model.NoteDetails
import nl.dekkr.eversync.scala.service.EvernoteBase


class Evernote extends EvernoteBase {

  def this(token: String) {
    this()
    login(token)
  }

  @throws(classOf[Exception])
  def createNote(noteDetails: NoteDetails) {
    // TODO select notebook
    // TODO add tags
    createNoteFromFile(noteDetails.filedetails.file, noteDetails.filedetails.mimeType, noteDetails.title, noteDetails.tags).map { case note =>
      println("Successfully created a new note with GUID: " + note)
      newNoteGuid = Some(note.getGuid)
      newNoteGuid
    }
  }


}
