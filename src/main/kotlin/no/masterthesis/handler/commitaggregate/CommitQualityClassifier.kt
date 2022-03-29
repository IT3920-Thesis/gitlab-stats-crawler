package no.masterthesis.handler.commitaggregate

import kotlin.math.max
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.changecontribution.ContributionType
import no.masterthesis.domain.commitaggregate.CommitAggregate
import no.masterthesis.service.gitlab.GitCommit
import no.masterthesis.util.ChangeContributionClassifier.predictContributionType
import org.slf4j.LoggerFactory

internal object CommitQualityClassifier {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Attempts to classify the size of a commit using the following algorithm:
   *
   * 1. Determine lines changed: max(lines added, lines removed)
   * 2. Check if commit has a lot of lines changed
   * 3. Check if few or many files has been "touched" in a commit
   *
   * Lines changed has higher precedence, because a commit with
   * 1000 lines changed but on only one file, should still be considered large.
   *
   * Note! These sizes are only used for general guidance, since there are a lot
   * of non-numerical factor that determines whether a commit is small, medium or large.
   * https://softwareengineering.stackexchange.com/questions/206979/how-big-should-a-single-commit-be.
   * */
  fun classifyCommitSize(commit: GitCommit): CommitAggregate.CommitSize {
    val filesChanged = commit.diffs.size
    val linesChanged = max(commit.stats.additions, commit.stats.deletions)

    // Change thresholds accordingly
    val size = when {
      linesChanged > 800 -> CommitAggregate.CommitSize.LARGE
      linesChanged > 100 -> CommitAggregate.CommitSize.MEDIUM
      filesChanged < 10 -> CommitAggregate.CommitSize.SMALL
      filesChanged > 25 -> CommitAggregate.CommitSize.LARGE
      else -> CommitAggregate.CommitSize.SMALL
    }

    log.trace("Commit size is classified", kv("commitSha", commit.id), kv("size", size))
    return size
  }

  /**
   * Determines the balance of test in a commit
   * */
  @Suppress("MagicNumber")
  fun classifyCommitTestBalance(commit: GitCommit): CommitAggregate.TestBalance {
    val (testChanges, otherChanges) = commit.diffs
      .map { predictContributionType(it) }
      .partition { it == ContributionType.TEST }

    // If no changes are present, then we consider it to be mixed
    if (testChanges.isEmpty() && otherChanges.isEmpty()) {
      return CommitAggregate.TestBalance.MIXED
    }

    val testPercentage = testChanges.size.toDouble() / (otherChanges.size + testChanges.size)
    log.trace(
      "Calculated test percentage for commit",
      kv("commitSha", commit.id),
      kv("testPercentage", testPercentage),
      kv("testChanges", testChanges.size),
      kv("otherChanges", otherChanges.size),
    )

    // Calculate using percentages in case tiny changes to a single file were made
    // (only necessary if we count lines added, and not files touched)
    return when {
      testPercentage > 0.99 -> CommitAggregate.TestBalance.PURE_TEST_COMMIT
      testPercentage < 0.01 -> CommitAggregate.TestBalance.NON_TEST_COMMIT
      else -> CommitAggregate.TestBalance.MIXED
    }
  }

  fun isMergeCommit(commit: GitCommit): Boolean {
    val title = commit.title

    // I've seen typos where the "M" in Merge was excluded,
    // so we only match on "erge branch"
    return title.contains("erge branch '", ignoreCase = true)
      && (title.contains("' into '") || title.contains("' of "))
  }


  fun extractIssueIdsReferenced(commit: GitCommit): Set<String> {
    val issuesInTitle = extractIssueIds(commit.title)
    val issuesInMessage = commit.message?.let { extractIssueIds(it) } ?: emptySet()

    return issuesInTitle.plus(issuesInMessage)
  }

  private fun extractIssueIds(message: String): Set<String> {
    val tokens = message
      .trim()
      .split("\n")
      .flatMap { it.split(" ") }

    log.trace("Tokens", kv("tokens", tokens))
    return tokens
      .map { it.trim() }
      .filter(::isIssueReference)
      .toSet()
  }

  /**
   * Matches on #xxx and <project-id>#xxx, such as #1, #12, or project-name#12
   * */
  private fun isIssueReference(word: String) = word.contains("#\\d{1,3}\$".toRegex())
}
