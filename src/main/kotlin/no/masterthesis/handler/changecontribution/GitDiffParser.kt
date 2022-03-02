package no.masterthesis.handler.changecontribution

import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.slf4j.LoggerFactory

@Singleton
internal class GitDiffParser {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * @return tuple of lines change: (<linesAdded>, <linesRemoved>)
   * */
  fun countLinesChanged(diff: GitlabGitCommitDiff): Pair<Int, Int> {
    log.trace("Counting lines changed in diff...", kv("diff", diff))

    val diffs = diff.diff.split("\n")

    val linesAdded = diffs.count { it.startsWith("+") }
    val linesRemoved = diffs.count { it.startsWith("-") }

    log.info("Changes has been counted", kv("linesAdded", linesAdded), kv("linesRemoved", linesRemoved), kv("diff", diff))
    return linesAdded to linesRemoved
  }
}
