package no.masterthesis.handler

import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.ApplicationStartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.event.GitlabCommitEvent
import no.masterthesis.service.gitlab.GitlabCommitCrawler
import no.masterthesis.service.gitlab.GitlabGroupCrawler
import org.slf4j.LoggerFactory

@Singleton
@Requires(notEnv = [Environment.TEST])
internal class GitlabCrawler(
  @Inject private val commitCrawler: GitlabCommitCrawler,
  @Inject private val groupCrawler: GitlabGroupCrawler,
  @Inject private val publisher: ApplicationEventPublisher<GitlabCommitEvent>,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Regular job that re-syncs with Gitlab every 24 hours,
   * and once on deployment
   * */
  @Scheduled(fixedDelay = "24h", initialDelay = "1m")
  fun crawlGitlabProjects() {
    val startTime = System.currentTimeMillis()
    // This base group represents the course
    val groupId = "it3920-gitlab-projects-examples"
    // These subgroups represent the group a team is member of
    val groups = runBlocking { groupCrawler.crawlGitlabGroup(groupId) }

    val commitsInProjects = groups.flatMap { subGroup ->
      log.info("Crawling projects in sub-group", kv("subGroup", subGroup))
      subGroup.projects.map { project ->
        val commits = commitCrawler.findAllCommitsByProject(project.id)
        Triple(subGroup, project, commits)
      }
    }

    // Every commit for every project has its own event
    val gitlabCommitEvents = commitsInProjects.flatMap { (subGroup, project, commits) ->
      commits.map { commit ->
        GitlabCommitEvent(
          groupId = subGroup.groupId,
          repositoryId = project.path,
          commit = commit,
        )
      }
    }

    gitlabCommitEvents.map { event ->
      log.trace(
        "Publishing event...",
        kv("commitSha", event.commit.id),
        kv("subGroupId", event.groupId),
        kv("projectPath", event.repositoryId),
      )
      publisher.publishEventAsync(event)
    }.map {
      // Wait for all event listeners to complete
      @Suppress("MagicNumber")
      it.get(60, TimeUnit.SECONDS)
    }

    val endTime = System.currentTimeMillis()
    val timeTakenSeconds = (endTime - startTime) / 1000
    log.info("Crawling completed", kv("timeTakenSeconds", timeTakenSeconds))
  }
}
