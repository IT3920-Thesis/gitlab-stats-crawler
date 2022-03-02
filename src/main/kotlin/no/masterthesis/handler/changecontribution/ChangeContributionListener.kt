package no.masterthesis.handler.changecontribution

import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.event.GitlabCommitEvent
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.slf4j.LoggerFactory

@Singleton
internal open class ChangeContributionListener {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   *
   * */
  @EventListener
  @Async
  open fun onCommit(event: GitlabCommitEvent) {
    log.info("Received new commit event", kv("event", event))

    val linesAdded = countLinesAdded(event.commit.diffs)
  }

  private fun countLinesAdded(diffs: List<GitlabGitCommitDiff>): Long {

    return 0
  }
}
