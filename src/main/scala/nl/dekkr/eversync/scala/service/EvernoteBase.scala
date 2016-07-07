package nl.dekkr.eversync.scala.service

import java.io.File

import com.evernote.auth.{EvernoteAuth, EvernoteService}
import com.evernote.clients.{ClientFactory, NoteStoreClient, UserStoreClient}
import com.evernote.edam.`type`.{Note, Resource, ResourceAttributes}
import nl.dekkr.eversync.scala.util.{ContentHelper, FileHelper}


class EvernoteBase {

  protected var userStore: UserStoreClient = null
  protected var noteStore: NoteStoreClient = null
  protected var newNoteGuid: Option[String] = None

  /**
    * Intialize UserStore and NoteStore clients. During this step, we
    * authenticate with the Evernote web service. All of this code is boilerplate
    * - you can copy it straight into your application.
    */
  @throws(classOf[Exception])
  def login(token: String) {
    //this()
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

  protected def createNoteFromFile(file: File, mimeType: String, title: String, tags : List[String] = List("EverSync")): Option[Note] = {
    val note: Note = new Note
    note.setTitle(title)
    FileHelper.readFileAsData(file.getAbsolutePath).map {
      case fileData =>
        val resource: Resource = new Resource
        resource.setData(fileData)
        resource.setMime(mimeType)
        val attributes: ResourceAttributes = new ResourceAttributes
        attributes.setFileName(file.getName)
        resource.setAttributes(attributes)
        note.addToResources(resource)
        val hashHex: String = FileHelper.bytesToHex(resource.getData.getBodyHash)
        note.setContent(
          ContentHelper.fillNoteTemplate(
            "<en-media type=\"" + mimeType + "\" hash=\"" + hashHex + "\"/>" + ""
          )
        )
        note.addToTagNames("EverSync")
        tags.foreach(note.addToTagNames)
        noteStore.createNote(note)

    }
  }


}
