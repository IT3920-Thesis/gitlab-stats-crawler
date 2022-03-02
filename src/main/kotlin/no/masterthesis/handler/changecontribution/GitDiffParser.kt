package no.masterthesis.handler.changecontribution

import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.slf4j.LoggerFactory

/**
 * Utility functions which parses the raw Git Diff file-format
 * */
internal object GitDiffParser {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Counts the number of lines added and removed from a file in a commit.
   * Files that are renamed will return (0, 0)
   *
   * @return tuple of lines change: (<linesAdded>, <linesRemoved>)
   * */
  fun countLinesChanged(diff: GitlabGitCommitDiff): Pair<Int, Int> {
    log.trace("Counting lines changed in diff...", kv("diff", diff))

    val diffs = diff.diff.split("\n")

    val linesAdded = diffs.count { it.startsWith("+") }
    val linesRemoved = diffs.count { it.startsWith("-") }

    log.trace("Changes has been counted", kv("linesAdded", linesAdded), kv("linesRemoved", linesRemoved), kv("diffBMode", diff.bMode))
    return linesAdded to linesRemoved
  }
}
