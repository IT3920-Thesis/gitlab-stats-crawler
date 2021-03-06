package no.masterthesis.service.gitlab

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import org.reactivestreams.Publisher

private const val GITLAB_API_CLIENT_PREFIX = "gitlabapi"
private const val GITLAB_MAX_ITEM_PER_PAGE = 100

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabErrorResponse(
  val message: String,
)

@Suppress("TooManyFunctions")
@Client(
  id = GITLAB_API_CLIENT_PREFIX,
  errorType = GitlabErrorResponse::class,
)
interface GitlabApiClient {
  /**
   * Retrieves the complete list of Git commits in the specified
   * Gitlab project.
   * @link https://docs.gitlab.com/ee/api/commits.html#list-repository-commits
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/commits?all=true&with_stats=true&per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
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

  /**
   * Lists all files (recursively) in a project.
   *
   * Note that folder names will because of this occur multiple times,
   * such as node_modules/bin/file1.js, node_modules/bin/file2.js
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/tree?recursive=true&per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listFilesInRepository(
    projectId: String,
    page: Int = 1,
  ): Publisher<List<GitlabFileSummary>>

  /**
   * Lists all issues for a project
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/issues/?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listIssuesInProject(
    projectId: Long,
    page: Int = 1,
  ): Publisher<List<GitlabIssue>>

  /**
   * Lists all notes (comments) on an issue
   *
   * @param projectId
   * @param issueIid (Not a typo) The iid from the issue
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/issues/{issueIid}/notes?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listNotesInIssue(
    projectId: Long,
    issueIid: Long,
    page: Int = 1,
  ): Publisher<List<GitlabNote>>

  /**
   * Lists all notes (comments) on an issue
   *
   * @param projectId
   * @param issueIid (Not a typo) The iid from the issue
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/milestones?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listProjectMilestones(
    projectId: Long,
    page: Int = 1,
  ): Publisher<List<GitlabMilestone>>

  /**
   * Lists all merge requests
   *
   * @param projectId
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/merge_requests?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listMergeRequests(
    projectId: Long,
    page: Int = 1,
  ): Publisher<List<GitlabMergeRequest>>

  /**
   * Lists all notes (comments) on an issue
   *
   * @param projectId
   * @param iId (Not a typo) The iid for a Merge Request
   * */
  @Get(
    uri = "/api/v4/projects/{projectId}/merge_requests/{iId}/notes?per_page=$GITLAB_MAX_ITEM_PER_PAGE&page={page}",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun listNotesInMergeRequest(
    projectId: Long,
    iId: Long,
    page: Int = 1,
  ): Publisher<List<GitlabNote>>
}
