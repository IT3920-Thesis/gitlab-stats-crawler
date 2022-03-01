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
internal class ChangeContributionClassifier {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val javaScriptEndings = setOf("js", "ts", "jsx", "tsx", "mjs")
  }

  /**
   * Predicts which contributions have been included a specific commit.
   * One commit can contribute to one or many [ContributionType]s.
   *
   * We use the word "predict", because this classification requires some
   * degree of guesswork.
   *
   * @return A list of predicted contributions to the [commit]
   * */
  fun predictContributionTypes(commit: GitCommit): List<Contribution> {
    log.trace("Predicting contributions for commit...", kv("commit", commit))
    val contributions = commit.diffs.flatMap { diff ->
      listOf(
        getTestContribution(diff),
        getFunctionalCode(diff),
      )
    }.filterNotNull()
    log.info("Contributions predicted", kv("commit", commit), kv("contributions", contributions))

    return contributions
  }

  private fun getTestContribution(diff: GitlabGitCommitDiff): Contribution? {
    val newPath = diff.newPath

    if (!isTestCode(newPath)) {
      return null
    }

    return Contribution(
      type = ContributionType.TEST,
      linesAdded = 15,
      linesRemoved = 15,
    )
  }

  private fun isTestCode(filename: String) = filename.startsWith("src/test/")
    // Jest tests
    || filename.contains("__tests__")
    || filename.contains(".spec.")
    || filename.contains(".test.")

  private fun getFunctionalCode(diff: GitlabGitCommitDiff): Contribution? {
    val filename = diff.newPath
    if (!isFunctionalCode(filename)) {
      return null
    }

    return Contribution(
      type = ContributionType.FUNCTIONAL,
      linesRemoved = 0,
      linesAdded = 0,
    )
  }

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
