package no.masterthesis.util

import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.changecontribution.ContributionType
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.slf4j.LoggerFactory

object ChangeContributionClassifier {
  private val log = LoggerFactory.getLogger(this::class.java)
  private val javaScriptEndings = setOf("js", "ts", "jsx", "tsx", "mjs")
  private val stylesheetEndings = setOf("less", "css", "sass", "scss")
  private val templateFiles = setOf("html", "twig")
  private val generalConfigurationExtensions = setOf("conf", "toml", "tml", "env", "properties", "xml")

  /**
   * Predicts which contributions have been included a specific commit.
   * One commit can contribute to one or many [ContributionType]s.
   *
   * We use the word "predict", because this classification requires some
   * degree of guesswork.
   *
   * @return A list of predicted contributions to a specific file change
   * */
  fun predictContributionType(diff: GitlabGitCommitDiff): ContributionType {
    log.trace("Predicting contributions for diff...", kv("newFile", diff.newPath))

    val contributionType = when {
      isTestCode(diff.newPath) -> ContributionType.TEST
      isConfigurationFile(diff.newPath) -> ContributionType.CONFIGURATION
      // This check has some broad checks, which is why it
      // should be after configuration and test
      isFunctionalCode(diff.newPath) -> ContributionType.FUNCTIONAL
      isDocumentationFile(diff.newPath) -> ContributionType.DOCUMENTATION
      else -> ContributionType.OTHER
    }
    log.trace("File diff is classified", kv("newPath", diff.newPath), kv("contributionType", contributionType))

    return contributionType
  }

  private fun isTestCode(filename: String) = filename.startsWith("src/test/")
    // Jest tests
    || filename.contains("__tests__")
    || filename.contains(".spec.")
    || filename.contains(".test.")
    // Cypress tests are often located inside this folder
    || filename.startsWith("cypress/")
    // This isn't really a test, but is highly related to the test setup
    || filename.endsWith("jest.config.js")

  @Suppress("ReturnCount")
  private fun isFunctionalCode(filename: String): Boolean {
    if (filename.startsWith("src/main/")) {
      log.trace("Predict that filename is Java Functional", kv("filename", filename))
      return true
    }

    // JavaScript source code is slightly more difficult to predict
    val isJavaScriptFile = javaScriptEndings.any { ending -> filename.endsWith(".$ending") }

    if (isJavaScriptFile && !isTestCode(filename)) {
      log.trace("Predict that filename is JS functional", kv("filename", filename))
      return true
    }

    val isTemplateFile = templateFiles.any { ending -> filename.endsWith(".$ending") }
    if (isTemplateFile) {
      log.trace("Predict that filename is a template file, functional code", kv("filename", filename))
      return true
    }

    val isStylesheetFile = stylesheetEndings.any { ending -> filename.endsWith(".$ending") }
    if (isStylesheetFile) {
      log.trace("Predict that filename is Stylesheet", kv("filename", filename))
      return true
    }

    val isBashScript = filename.endsWith(".sh")
    if (isBashScript) {
      return true
    }

    val isSqlFile = filename.endsWith(".sql")
    if (isSqlFile) {
      return true
    }

    return false
  }

  private fun isDocumentationFile(fileName: String): Boolean {
    // Should also catch the readme
    if (fileName.endsWith(".md")) {
      return true
    }

    // Assume that files inside docs are documentation code as well
    if (fileName.startsWith("docs/") || fileName.startsWith("documentation/")) {
      return true
    }
    return false
  }

  @Suppress("ReturnCount")
  private fun isConfigurationFile(fileName: String): Boolean {
    if (CodeQualityParser.isLinterConfig(fileName)) {
      return true
    }

    // Some Dockerfiles are sometimes suffixed with an ending
    // which is why we match using .contains
    val isDockerFile = fileName.contains("Dockerfile")
      || fileName.endsWith(".dockerignore")

    if (isDockerFile) {
      return true
    }

    val isEditorConfig = fileName.endsWith(".editorconfig")
    if (isEditorConfig) {
      return true
    }

    val isGitConfig = fileName.endsWith(".gitignore")
      || fileName.endsWith(".gitattributes")
      || fileName.endsWith(".gitmodules")

    if (isGitConfig) {
      return true
    }

    val isJavaBuildConfig = fileName.endsWith(".gradle", true)
      // Normally used for kotlin configurations, instead of .gradle
      || fileName.endsWith(".kts", true)
      || fileName.endsWith("pom.xml", true)

    if (isJavaBuildConfig) {
      return true
    }

    val isGeneralConfigurationExtension = generalConfigurationExtensions.any { extension ->
      fileName.endsWith(".$extension")
    }

    // Generic configuration file
    if (isJsonFile(fileName) || isYamlFile(fileName) || isGeneralConfigurationExtension) {
      return true
    }

    return false
  }

  private fun isJsonFile(fileName: String) = fileName.endsWith(".json")
    || fileName.endsWith(".json5") // JSON file using the JSON5 spec
    || fileName.endsWith(".ndjson") // JSON where each item is a newline

  private fun isYamlFile(fileName: String) = fileName.endsWith(".yml", true)
    || fileName.endsWith(".yaml", true)
}
