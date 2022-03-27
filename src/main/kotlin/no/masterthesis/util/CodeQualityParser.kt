package no.masterthesis.util

object CodeQualityParser {

  fun isLinterConfig(fileName: String): Boolean {
    // The eslint config file can have many suffixes
    // See https://eslint.org/docs/user-guide/configuring/configuration-files
    val isEsLint = fileName.contains(".eslintrc", true)
      || fileName.endsWith(".eslintignore", true)

    if (isEsLint) {
      return true
    }

    val isPrettierConfig = fileName.contains(".prettierrc", true)
      || fileName.contains("prettier.config")

    if (isPrettierConfig) {
      return true
    }

    // These are simple checks for whether a checkstyle file is present.
    // The later check is just guesswork, for what may be a checkstyle configuration
    val isCheckStyle = fileName.contains("checkstyle.xml") || fileName.endsWith("_check.xml")
    if (isCheckStyle) {
      return true
    }

    return false
  }
}
