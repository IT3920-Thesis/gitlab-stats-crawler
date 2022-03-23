package no.masterthesis.service.gitlab

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

data class GitlabAggregatedGroup(
  val baseGroupPath: String,
  val groupId: String,
  val projects: List<GitlabProject>,
)

@Singleton
class GitlabGroupCrawler(
  @Inject private val client: GitlabApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Crawls the group for subgroups and their internal projects.
   * It only goes one level deep. Therefore, project inside a subgroup's subgroup
   * is not found
   * */
  suspend fun crawlGitlabGroup(groupPath: String): List<GitlabAggregatedGroup> {
    val subgroups = client.listSubGroups(groupPath).awaitSingle()
    log.trace("Found subgroups", kv("subGroups", subgroups))

    val aggregatedGroups = subgroups
      .map {
        log.info("Retrieving projects for subgroup", kv("subgroup", it))
        Thread.sleep(300) // This used to reduce the immediate strain on gitlab
        GitlabAggregatedGroup(
          baseGroupPath = groupPath,
          groupId = it.path,
          projects = client.listProjectsInGroup(it.id.toString()).awaitSingle(),
        )
      }

    log.trace("Identified projects in subgroup", kv("aggregatedGroups", aggregatedGroups))

    return aggregatedGroups
  }
}
