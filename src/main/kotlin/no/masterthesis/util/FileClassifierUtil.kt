package no.masterthesis.util

object FileClassifierUtil {
  val buildFolders = setOf(
    "target",
    ".gradle",
    "build",
    "node_modules",
    "dist",
    "venv/",
  )

  val dataFiles = setOf(
    "db.sqlite3",
    "db.sqlite3-journal",
  )

  fun isInBuildFolder(filePath: String) = buildFolders.any { folder -> filePath.contains(folder) }
}
