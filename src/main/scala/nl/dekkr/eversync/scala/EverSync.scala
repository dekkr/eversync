package nl.dekkr.eversync.scala

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.security.MessageDigest
import java.util.{List => JList}

import com.evernote.auth.{EvernoteAuth, EvernoteService}
import com.evernote.clients.{ClientFactory, NoteStoreClient, UserStoreClient}
import com.evernote.edam.`type`.{Data, Note, NoteSortOrder, Notebook, Resource, ResourceAttributes, Tag}
import com.evernote.edam.error.{EDAMErrorCode, EDAMSystemException, EDAMUserException}
import com.evernote.edam.notestore.{NoteFilter, NoteList}
import com.evernote.thrift.transport.TTransportException

import scala.collection.JavaConversions._
import java.nio.file.{Paths, Files}
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
  val demo: EverSync = new EverSync(token)
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

  //}

  /**
    * Helper method to read the contents of a file on disk and create a new Data
    * object.
    */
  @throws(classOf[Exception])
  private def readFileAsData(fileName: String): Option[Data] = {
    if (Files.exists(Paths.get(fileName))) {
    val filePath: String = new File(classOf[EverSync].getResource(classOf[EverSync].getCanonicalName + ".class").getPath).getParent + File.separator + fileName
    val in: FileInputStream = new FileInputStream(filePath)
    val byteOut: ByteArrayOutputStream = new ByteArrayOutputStream
    val block: Array[Byte] = new Array[Byte](10240)
    var len: Int = 0
    while ({
      len = in.read(block)
      len
    } >= 0) {
      byteOut.write(block, 0, len)
    }
    in.close()
    val body: Array[Byte] = byteOut.toByteArray
    val data: Data = new Data
    data.setSize(body.length)
    data.setBodyHash(MessageDigest.getInstance("MD5").digest(body))
    data.setBody(body)
    Some(data)
    } else {
      None
    }
  }

  /**
    * Helper method to convert a byte array to a hexadecimal string.
    */
  def bytesToHex(bytes: Array[Byte]): String = {
    val sb: StringBuilder = new StringBuilder
    for (hashByte <- bytes) {
      val intVal: Int = 0xff & hashByte
      if (intVal < 0x10) {
        sb.append('0')
      }
      sb.append(Integer.toHexString(intVal))
    }
    sb.toString
  }
}

class EverSync {
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
  private def listNotes() {
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
    * Create a new note containing a little text and the Evernote icon.
    */
  @throws(classOf[Exception])
  private def createNote() {
    val note: Note = new Note
    note.setTitle("Test note from EverSync")
    val fileName: String = "enlogo.png"
    val mimeType: String = "image/png"
    EverSync.readFileAsData(fileName) match {
      case Some(fileData) =>
        val resource: Resource = new Resource
        resource.setData(fileData)
        resource.setMime(mimeType)
        val attributes: ResourceAttributes = new ResourceAttributes
        attributes.setFileName(fileName)
        resource.setAttributes(attributes)
        note.addToResources(resource)
        val hashHex: String = EverSync.bytesToHex(resource.getData.getBodyHash)
        val content: String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" + "<en-note>" + "<span style=\"color:green;\">Here's the Evernote logo:</span><br/>" + "<en-media type=\"image/png\" hash=\"" + hashHex + "\"/>" + "</en-note>"
        note.setContent(content)
      case None =>
        println("File not found; not included in note")
        val content: String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" + "<en-note>" + "<span style=\"color:green;\">No logo file found</span>" + "</en-note>"
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
  private def searchNotes() {
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
  private def updateNoteTag() {
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