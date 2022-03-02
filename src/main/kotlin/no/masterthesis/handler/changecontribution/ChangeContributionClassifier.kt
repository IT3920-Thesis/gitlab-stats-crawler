package no.masterthesis.handler.changecontribution

import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.changecontribution.ContributionType
import no.masterthesis.service.gitlab.GitCommit
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.slf4j.LoggerFactory

internal data class Contribution(
  val linesAdded: Long,
  val linesRemoved: Long,
  val type: ContributionType,
)

@Singleton
internal object ChangeContributionClassifier {
  private val log = LoggerFactory.getLogger(this::class.java)
  private val javaScriptEndings = setOf("js", "ts", "jsx", "tsx", "mjs")

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
    log.trace("Predicting contributions for diff...", kv("diff", diff))

    if (isTestCode(diff.bMode)) {
      log.trace("File diff is classified as contribution to tests", kv("diff", diff))
      return ContributionType.TEST
    }

    if (isFunctionalCode(diff.bMode)) {
      log.trace("File diff is classified as contribution to functional code", kv("diff", diff))
      return ContributionType.FUNCTIONAL
    }

    log.warn("Could not classify the contribution to any specific types", kv("aMode", diff.aMode), kv("bMode", diff.bMode))
    return ContributionType.OTHER
  }

  private fun isTestCode(filename: String) = filename.startsWith("src/test/")
    // Jest tests
    || filename.contains("__tests__")
    || filename.contains(".spec.")
    || filename.contains(".test.")

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

    return false
  }
}
