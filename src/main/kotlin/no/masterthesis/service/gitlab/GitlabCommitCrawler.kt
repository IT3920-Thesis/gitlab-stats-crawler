package no.masterthesis.service.gitlab

import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.util.FileClassifierUtil
import org.slf4j.LoggerFactory

data class GitCommit(
  val id: String,
  val createdAt: ZonedDateTime,
  val title: String,
  val message: String?,
  val committer: Author,
  val diffs: List<GitlabGitCommitDiff>,
  val stats: GitlabGitCommit.Stats,
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
open class GitlabCommitCrawler(
  @Inject private val client: GitlabApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Retrieves all commits for a project, and changes of each commit
   * @param projectId The numeric project id in Gitlab (make sure you have access to this project)
   * @return List of commits with associated differences
   * */
  @Cacheable("commits", parameters = ["projectId"])
  open fun findAllCommitsByProject(projectId: Long): List<GitCommit> = runBlocking {
    val commits = retrieveAllCommits(projectId)
    log.info(
      "Commits in project. Extracting diffs...",
      kv("projectId", projectId),
      kv("numberOfCommits", commits.size),
    )

    log.trace("Extracting file diffs from every commit", kv("projectId", projectId), kv("numberOfCommits", commits.size))
    // Retrieve all diffs in one go, so we can make use of concurrency
    val commitDiffs = retrieveAllDiffs(
      projectId = projectId,
      commitSha = commits.keys
    )
    log.info(
      "Diffs extracted from project",
      kv("projectId", projectId),
      kv("numberOfCommits", commits.size),
      kv("numberOfDiffs", commitDiffs.values.sumOf { it.size }),
    )

    commitDiffs.map { (commitSha, diffs) ->
      val commitMetadata = commits.getValue(commitSha)
      log.trace("Commit has number of diffs", kv("commitSha", commitSha), kv("numberOfDiffs", diffs.size))

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
        stats = commitMetadata.stats,
      )
    }
  }

  private fun retrieveAllDiffs(projectId: Long, commitSha: Set<String>): Map<String, List<GitlabGitCommitDiff>> {
    val diffs = commitSha
      .chunked(100)
      .flatMap { commits ->
        runBlocking {
          delay(5)
          commits.map { sha -> sha to retrieveDiffsForCommit(projectId, sha) }
        }
      }
      .associate { it.first to it.second }

    return diffs
  }

  private suspend fun retrieveDiffsForCommit(projectId: Long, commitSha: String): List<GitlabGitCommitDiff> {
    val allDiffs = ArrayList<GitlabGitCommitDiff>()
    var currentPage = 1

    while (true) {
      log.trace("Requesting diff for commit", kv("projectId", projectId), kv("commitSha", commitSha), kv("page", currentPage))
      val page = client
        .findCommitDiffs(
          projectId = projectId,
          commitSha = commitSha,
          page = currentPage,
        )
        .awaitSingle()
        // Build folders can be massive (such as node_modules), and are discarded to save space
        .filter { !FileClassifierUtil.isInBuildFolder(it.newPath) }

      // Continue until gitlab has no more data to return
      if (page.isEmpty()) {
        break
      }
      allDiffs.addAll(page)
      currentPage += 1
    }

    return allDiffs
  }

  private fun retrieveAllCommits(projectId: Long): Map<String, GitlabGitCommit> = runBlocking {
    val allCommits = ArrayList<GitlabGitCommit>()
    var currentPage = 1

    while (true) {
      log.trace("Requesting commit", kv("projectId", projectId), kv("page", currentPage))
      val page = client.findAllCommitsByProject(projectId, page = currentPage).awaitSingle()
      // Continue until gitlab has no more data to return
      if (page.isEmpty()) {
        break
      }
      allCommits.addAll(page)
      currentPage += 1
    }

    allCommits.associateBy { it.id }
  }
}
