package nl.dekkr.eversync.scala.depricated

import java.util.{List => JList}

import com.evernote.auth.{EvernoteAuth, EvernoteService}
import com.evernote.clients.{ClientFactory, NoteStoreClient, UserStoreClient}
import com.evernote.edam.`type`.{Note, NoteSortOrder, Notebook, Resource, ResourceAttributes, Tag}
import com.evernote.edam.notestore.{NoteFilter, NoteList}
import nl.dekkr.eversync.scala.util.{FileHelper, ContentHelper}

import scala.collection.JavaConversions._


@deprecated("For reference included, to be replaced by own implementation")
class EvernoteDemo {
    /** *************************************************************************
      * You shouldn't need to change anything below here to run sample code *
      * **************************************************************************/
    private var userStore: UserStoreClient = null
    private var noteStore: NoteStoreClient = null
    private var newNoteGuid: String = null

    /**
      * Intialize UserStore and NoteStore clients. During this step, we
      * authenticate with the Evernote web service. All of this code is boilerplate
      * - you can copy it straight into your application.
      */
    @throws(classOf[Exception])
    def this(token: String) {
      this()
      val evernoteAuth: EvernoteAuth = new EvernoteAuth(EvernoteService.SANDBOX, token)
      val factory: ClientFactory = new ClientFactory(evernoteAuth)
      userStore = factory.createUserStoreClient
      val versionOk: Boolean = userStore.checkVersion("EverSync (Scala)", com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR, com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR)
      if (!versionOk) {
        System.err.println("Incompatible Evernote client protocol version")
        System.exit(1)
      }
      noteStore = factory.createNoteStoreClient
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
      val note: Note = new Note
      note.setTitle("Test note from EverSync")
      val fileName: String = "enlogo.png"
      val mimeType: String = "image/png"
      FileHelper.readFileAsData(fileName) match {
        case Some(fileData) =>
          val resource: Resource = new Resource
          resource.setData(fileData)
          resource.setMime(mimeType)
          val attributes: ResourceAttributes = new ResourceAttributes
          attributes.setFileName(fileName)
          resource.setAttributes(attributes)
          note.addToResources(resource)
          val hashHex: String = FileHelper.bytesToHex(resource.getData.getBodyHash)
          val content: String = ContentHelper.fillNoteTemplate(
            "<span style=\"color:green;\">Here's the Evernote logo:</span><br/>" +
              "<en-media type=\"" + mimeType + "\" hash=\"" + hashHex + "\"/>" + ""
          )
          note.setContent(content)
        case None =>
          println("File not found; not included in note")
          val content: String = ContentHelper.fillNoteTemplate("<span style=\"color:green;\">No logo file found</span>")
          note.setContent(content)
      }
      val createdNote: Note = noteStore.createNote(note)
      newNoteGuid = createdNote.getGuid
      println("Successfully created a new note with GUID: " + newNoteGuid)
      println()
    }

    /**
      * Search a user's notes and display the results.
      */
    @throws(classOf[Exception])
    def searchNotes() {
      val query: String = "intitle:EDAMDemo"
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
      var note: Note = noteStore.getNote(newNoteGuid, true, true, false, false)
      note.unsetContent()
      note.unsetResources()
      note.addToTagNames("TestTag")
      noteStore.updateNote(note)
      println("Successfully added tag to existing note")
      note = noteStore.getNote(newNoteGuid, false, false, false, false)
      println("After update, note has " + note.getResourcesSize + " resource(s)")
      println("After update, note tags are: ")
      import scala.collection.JavaConversions._
      for (tagGuid <- note.getTagGuids) {
        val tag: Tag = noteStore.getTag(tagGuid)
        println("* " + tag.getName)
      }
    }



}
