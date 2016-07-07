package nl.dekkr.eversync.scala.util

import java.io.File

import scala.sys.process._
import scala.util.{Success, Try}

object Mimetype {

  def mimeType(file: File): Try[String] = {
    var retValue: String = ""

    Seq("mimetype", "-b", file.getAbsolutePath) ! ProcessLogger(line => retValue = line) match {
      case ret if ret == 0 => Success(retValue)
      case _ => throw new Exception("Unable to determine mime-type")
    }
  }

}