package no.masterthesis.handler

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.ApplicationStartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.event.GitlabCommitEvent
import no.masterthesis.service.gitlab.GitlabCommitCrawler
import org.slf4j.LoggerFactory

@Singleton
internal class GitlabCrawler(
  @Inject private val crawler: GitlabCommitCrawler,
  @Inject private val publisher: ApplicationEventPublisher<GitlabCommitEvent>,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @EventListener
  fun crawlGitlabProjects(event: ApplicationStartupEvent) {
    val groupId = "Prosjekt4"
    val projectId = 1021L
    val commits = crawler.findAllCommitsByProject(projectId)

    commits.map {
      log.info("Publishing event...", kv("commit", it))
      publisher.publishEventAsync(GitlabCommitEvent(
        groupId = groupId,
        commit = it,
      ))
    }
  }
}