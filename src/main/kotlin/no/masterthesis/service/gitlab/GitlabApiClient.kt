package no.masterthesis.service.gitlab

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import org.reactivestreams.Publisher

private const val GITLAB_API_CLIENT_PREFIX = "gitlabapi"

@Client(id = GITLAB_API_CLIENT_PREFIX)
interface GitlabApiClient {
  /**
   * Retrieves the complete list of Git commits in the specified
   * Gitlab project.
   * @link https://docs.gitlab.com/ee/api/commits.html#list-repository-commits
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/commits?all=true",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun findAllCommitsByProject(projectId: Long): Publisher<List<GitlabGitCommit>>

  /**
   * Retrieves a list of changes to a specific commit
   * @link https://docs.gitlab.com/ee/api/commits.html#get-the-diff-of-a-commit
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/commits/{commitSha}/diff",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun findCommitDiffs(projectId: Long, commitSha: String): Publisher<List<GitlabGitCommitDiff>>
}
