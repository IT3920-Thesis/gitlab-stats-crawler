package no.masterthesis.handler

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.context.env.Environment
import io.micronaut.discovery.event.ServiceReadyEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.service.gitlab.GitlabGroupCrawler
import org.slf4j.LoggerFactory

@Singleton
@Requires(notEnv = [Environment.TEST])
internal open class GitlabCrawler(
  @Inject private val groupCrawler: GitlabGroupCrawler,
  @Inject private val gitlabProjectClient: GitlabProjectClient,
  /**
   * @var A root group in GitLab which represents a "course" (and the semester it is taught)
   * */
  @param:Value("\${gitlabapi.courseRootGroupPath}") private val courseRootGroupName: String,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * This job crawls gitlab for subgroups and their projects inside a [courseRootGroupName].
   * Inside we expect that each subgroup represents a "Student Group",
   * where students have one or multiple repositories.
   *
   * Each project is published to RabbitMQ as [GitlabCrawlProjectEvent],
   * which other crawlers and parsers subscribes to.
   *
   * ```
   * rootGroup (Course, isolated to the semester it is taught)
   *   | studentGroup1
   *     | project1
   *     | project2
   *     ...
   *     | projectN
   *   | studentGroup2
   *     | project1
   *   ...
   *   | studentGroupN
   * ```
   *
   * */
  @EventListener
  @Async
  open fun crawlGitlab(event: ServiceReadyEvent) {
    log.info("Application has started. Crawling for gitlab metadata...", kv("courseRootGroupName", courseRootGroupName))
    // These subgroups represent the group a team is member of
    val subGroups = runBlocking { groupCrawler.crawlGitlabGroup(courseRootGroupName) }

    @Suppress("MagicNumber")
    subGroups.map { subGroup ->
      subGroup
        .projects
        // Archived projects can we discard immediately
        .filter { !it.isArchived }
        .map { project ->
          gitlabProjectClient.publish(GitlabCrawlProjectEvent(
            rootGroupId = courseRootGroupName,
            subGroupId = subGroup.groupId,
            projectPath = project.path,
            projectId = project.id,
            defaultBranch = project.defaultBranch,
          ))
        }
    }
    log.info("Groups crawled and published to exchange")
  }
}
