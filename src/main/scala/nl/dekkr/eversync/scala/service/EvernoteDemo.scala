package nl.dekkr.eversync.scala.service

import java.util.{List => JList}

import com.evernote.edam.`type`.{Note, NoteSortOrder, Notebook, Tag}
import com.evernote.edam.notestore.{NoteFilter, NoteList}

import scala.collection.JavaConversions._


@deprecated("For reference included, to be replaced by own implementation","No guarantee it works")
class EvernoteDemo extends  EvernoteBase {

  def this(token: String) {
    this()
    login(token)
  }

    /**
      * Retrieve and display a list of the user's notes.
      */
    @throws(classOf[Exception])
    def listNotes() {
      println("Listing notes:")
      val notebooks: JList[Notebook] = noteStore.listNotebooks
      for (notebook <- notebooks) {
        println("Notebook: " + notebook.getName)
        val filter: NoteFilter = new NoteFilter
        filter.setNotebookGuid(notebook.getGuid)
        filter.setOrder(NoteSortOrder.CREATED.getValue)
        filter.setAscending(true)
        val noteList: NoteList = noteStore.findNotes(filter, 0, 100)
        val notes: JList[Note] = noteList.getNotes
        for (note <- notes) {
          println(" * " + note.getTitle)
        }
      }
      println()
    }


    /**
      * Create a new note
      */
    @throws(classOf[Exception])
    def createNote() {
//      createNoteFromFile("enlogo.png", "image/png", "Test note from EverSync").map { case note =>
//        println("Successfully created a new note with GUID: " + note)
//        newNoteGuid = Some(note.getGuid)
//        newNoteGuid
//      }
    }

    /**
      * Search a user's notes and display the results.
      */
    @throws(classOf[Exception])
    def searchNotes() {
      val query: String = "intitle:EverSync"
      val filter: NoteFilter = new NoteFilter
      filter.setWords(query)
      filter.setOrder(NoteSortOrder.UPDATED.getValue)
      filter.setAscending(false)
      System.out.println("Searching for notes matching query: " + query)
      val notes: NoteList = noteStore.findNotes(filter, 0, 50)
      System.out.println("Found " + notes.getTotalNotes + " matching notes")
      notes.getNotesIterator.foreach { note =>
        System.out.println("Note: " + note.getTitle)
        val fullNote: Note = noteStore.getNote(note.getGuid, true, true, false, false)
        println("Note contains " + fullNote.getResourcesSize + " resources")
      }
    }

    /**
      * Update the tags assigned to a note. This method demonstrates how only
      * modified fields need to be sent in calls to updateNote.
      */
    @throws(classOf[Exception])
    def updateNoteTag() {
      newNoteGuid.map { noteUID =>
        var note: Note = noteStore.getNote(noteUID, true, true, false, false)
        note.unsetContent()
        note.unsetResources()
        note.addToTagNames("TestTag")
        noteStore.updateNote(note)
        println("Successfully added tag to existing note")
        note = noteStore.getNote(noteUID, false, false, false, false)
        println("After update, note has " + note.getResourcesSize + " resource(s)")
        println("After update, note tags are: ")
        import scala.collection.JavaConversions._
        for (tagGuid <- note.getTagGuids) {
          val tag: Tag = noteStore.getTag(tagGuid)
          println("* " + tag.getName)
        }
        noteUID
      }
    }



}
