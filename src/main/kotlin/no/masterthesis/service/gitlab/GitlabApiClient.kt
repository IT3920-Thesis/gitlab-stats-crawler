package no.masterthesis.service.gitlab

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import java.time.ZonedDateTime
import org.reactivestreams.Publisher

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabGitCommit(
  val id: String,
  val title: String,
  val authorName: String,
  val authorEmail: String?,
  // We might need to add some additional packages to Jackson
  val authoredDate: ZonedDateTime,
  val committerName: String?,
  val committerEmail: String?,
  val committedDate: ZonedDateTime,
  val createdAt: ZonedDateTime,
  val parentIds: List<String>,
  val webUrl: String?,
)

private const val GITLAB_API_CLIENT_PREFIX = "gitlabapi"

@Client(id = GITLAB_API_CLIENT_PREFIX)
interface GitlabApiClient {
  @Get(
    uri = "/api/v4/projects/{projectId}/repository/commits",
    consumes = [MediaType.APPLICATION_JSON],
  )
  fun findCommitsByProject(projectId: Long): Publisher<List<GitlabGitCommit>>
}
