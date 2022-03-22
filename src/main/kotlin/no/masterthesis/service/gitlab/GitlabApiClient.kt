package no.masterthesis.service.gitlab

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import org.reactivestreams.Publisher

private const val GITLAB_API_CLIENT_PREFIX = "gitlabapi"
private const val GITLAB_MAX_ITEM_PER_PAGE = 100

@Client(id = GITLAB_API_CLIENT_PREFIX)
interface GitlabApiClient {
  /**
   * Retrieves the complete list of Git commits in the specified
   * Gitlab project.
   * @link https://docs.gitlab.com/ee/api/commits.html#list-repository-commits
   *
   * TODO(fredrfli) Implement pagination, since gitlab api returns max 100 items
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/commits?all=true&per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun findAllCommitsByProject(projectId: Long, page: Int = 1): Publisher<List<GitlabGitCommit>>

  /**
   * Retrieves a list of changes to a specific commit
   * @link https://docs.gitlab.com/ee/api/commits.html#get-the-diff-of-a-commit
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/commits/{commitSha}/diff?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun findCommitDiffs(projectId: Long, commitSha: String, page: Int = 1): Publisher<List<GitlabGitCommitDiff>>

  /**
   * Retrieves a list of changes to a specific commit
   * @link https://docs.gitlab.com/ee/api/commits.html#get-the-diff-of-a-commit
   * */
  @Get(
    uri = "/api/v4/groups/{groupId}/?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun findGroupDetails(groupId: String, page: Int = 1): Publisher<GitlabGroupDetails>

  @Get(
    uri = "/api/v4/groups/{groupId}/subgroups/?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listSubGroups(groupId: String, page: Int = 1): Publisher<List<GitlabGroupDetails>>

  /**
   * Retrieves a list of changes to a specific commit
   * @link https://docs.gitlab.com/ee/api/commits.html#get-the-diff-of-a-commit
   * */
  @Get(
    uri = "/api/v4/groups/{groupId}/projects/?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listProjectsInGroup(groupId: String, page: Int = 1): Publisher<List<GitlabProject>>

  /**
   * Retrieves a specific file on gitlab, based on the specific files
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/files/{filePath}?ref={ref}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun retrieveFileFromRepository(
    projectId: String,
    filePath: String,
    ref: String = "master",
  ): Publisher<GitlabFile>
}
