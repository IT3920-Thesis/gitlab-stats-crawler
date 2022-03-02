package no.masterthesis.service.gitlab

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.ZonedDateTime
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

data class GitCommit(
  val id: String,
  val createdAt: ZonedDateTime,
  val title: String,
  val message: String?,
  val committer: Author,
  val diffs: List<GitlabGitCommitDiff>,
) {
  data class Author(
    val name: String?,
    val email: String,
  )
}

/**
 * Service that crawls Gitlab for commit information
 * */
@Singleton
class GitlabCommitCrawler(
  @Inject private val client: GitlabApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Retrieves all commits for a project, and changes of each commit
   * @param projectId The numeric project id in Gitlab (make sure you have access to this project)
   * @return List of commits with associated differences
   * */
  fun findAllCommitsByProject(projectId: Long): List<GitCommit> = runBlocking {
    val commits = client
      .findAllCommitsByProject(projectId).awaitSingle()
      .associateBy { it.id }
    log.info("All commits have been extracted for project", kv("projectId", projectId), kv("commits", commits.values))

    val commitDiffs = commits
      .mapValues {
        client.findCommitDiffs(
          projectId = projectId,
          commitSha = it.key,
        ).awaitSingle()
      }

    commitDiffs.map { (commitSha, diffs) ->
      val commitMetadata = commits.getValue(commitSha)

      GitCommit(
        id = commitSha,
        createdAt = commitMetadata.createdAt,
        title = commitMetadata.title,
        message = commitMetadata.message,
        committer = GitCommit.Author(
          name = commitMetadata.committerName,
          email = commitMetadata.committerEmail,
        ),
        diffs = diffs,
      )
    }
  }
}
