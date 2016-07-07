package nl.dekkr.eversync.scala.util

import com.typesafe.config.ConfigFactory


object Config {
    private val config =  ConfigFactory.load()

    private lazy val root = config.getConfig("eversync")

    lazy val auth_token = root.getString("auth_token")

    lazy val importDirectory = root.getString("import_directory")

    lazy val archiveAfterUpload = root.getBoolean("archive_after_upload")
    lazy val archiveDirectory = root.getString("archive_directory")
}


