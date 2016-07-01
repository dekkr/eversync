package nl.dekkr.eversync.scala.util

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.nio.file.{Files, Paths}
import java.security.MessageDigest

import com.evernote.edam.`type`.Data
import nl.dekkr.eversync.scala.depricated.EvernoteDemo

object FileHelper {

  /**
    * Helper method to read the contents of a file on disk and create a new Data
    * object.
    */
  @throws(classOf[Exception])
  def readFileAsData(fileName: String): Option[Data] = {
    if (Files.exists(Paths.get(fileName))) {
      val filePath: String = new File(classOf[EvernoteDemo].getResource(classOf[EvernoteDemo].getCanonicalName + ".class").getPath).getParent + File.separator + fileName
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
