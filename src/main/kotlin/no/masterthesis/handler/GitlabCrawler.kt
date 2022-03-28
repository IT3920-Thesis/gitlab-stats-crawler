package no.masterthesis.handler

import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.discovery.event.ServiceReadyEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.event.GitlabCommitEvent
import no.masterthesis.service.gitlab.GitlabAggregatedGroup
import no.masterthesis.service.gitlab.GitlabCommitCrawler
import no.masterthesis.service.gitlab.GitlabGroupCrawler
import org.slf4j.LoggerFactory

@Singleton
@Requires(notEnv = [Environment.TEST])
internal open class GitlabCrawler(
  @Inject private val commitCrawler: GitlabCommitCrawler,
  @Inject private val groupCrawler: GitlabGroupCrawler,
  @Inject private val publisher: ApplicationEventPublisher<GitlabCommitEvent>,
  @Inject private val gitlabProjectClient: GitlabProjectClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  companion object {
    /**
     * @var A root group in GitLab which represents a "course" (and the semester it is taught)
     * */
    private const val COURSE_ROOT_GROUP_NAME = "it3920-gitlab-projects-examples"
  }

  /**
   * This job crawls gitlab for subgroups and their projects inside a [COURSE_ROOT_GROUP_NAME].
   * Inside we expect that each subgroup represents a "Student Group",
   * where students have one or multiple repositories.
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
    log.info("Application has started. Crawling for gitlab metadata...")
    // These subgroups represent the group a team is member of
    val subGroups = runBlocking { groupCrawler.crawlGitlabGroup(COURSE_ROOT_GROUP_NAME) }

    subGroups.map { subGroup ->
      subGroup
        .projects
        // Archived projects can we discard immediately
        .filter { !it.isArchived }
//        .filter { it.id == 15903L } // TODO(fredrfli) Remove, so we can crawl all sites
        .map { project ->
          gitlabProjectClient.publish(GitlabCrawlProjectEvent(
            rootGroupId = COURSE_ROOT_GROUP_NAME,
            subGroupId = subGroup.groupId,
            projectPath = project.path,
            projectId = project.id,
            defaultBranch = project.defaultBranch,
          ))
        }
    }
    log.info("Groups crawled and published to exchange")
  }

  private fun extractDiffsInGroups(rootGroupId: String, groups: List<GitlabAggregatedGroup>) {
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
          rootGroupId = rootGroupId,
          groupId = subGroup.groupId,
          repositoryPath = project.path,
          projectId = project.id,
          defaultBranch = project.defaultBranch,
          commit = commit,
        )
      }
    }

    gitlabCommitEvents.map { event ->
      log.trace(
        "Publishing event...",
        kv("commitSha", event.commit.id),
        kv("subGroupId", event.groupId),
        kv("projectPath", event.repositoryPath),
      )
      publisher.publishEventAsync(event)
    }.map {
      // Wait for all event listeners to complete
      @Suppress("MagicNumber")
      it.get(60, TimeUnit.SECONDS)
    }

  }
}
