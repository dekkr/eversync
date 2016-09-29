package nl.dekkr.eversync.scala.service

import nl.dekkr.eversync.scala.model.NoteDetails


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
      log.debug("Successfully created a new note with GUID: " + note)
      newNoteGuid = Some(note.getGuid)
      newNoteGuid
    }
  }


}
