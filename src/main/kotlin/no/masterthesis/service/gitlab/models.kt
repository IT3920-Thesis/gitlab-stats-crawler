package no.masterthesis.service.gitlab

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Base64

/**
 * This file contains a mapping of Gitlab's API response structures,
 * into deserializable data classes.
 * */

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabUser(
  val state: String,
  val username: String,
  val id: Long,
)

/**
 * @property id Git commit SHA
 * @property title Commit message
 * */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabGitCommit(
  val id: String,
  val title: String,
  val message: String?,
  val authorName: String,
  val authorEmail: String,
  val authoredDate: ZonedDateTime,
  val committerName: String?,
  val committerEmail: String,
  val committedDate: ZonedDateTime,
  val createdAt: ZonedDateTime,
  val parentIds: List<String>,
  val webUrl: String?,
  val stats: Stats,
) {
  data class Stats(
    val additions: Int,
    val deletions: Int,
    val total: Int
  )
}

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabGitCommitDiff(
  val oldPath: String,
  val newPath: String,
  val aMode: String,
  val bMode: String,
  @JsonProperty("new_file")
  val isNewFile: Boolean,
  @JsonProperty("renamed_file")
  val isFileRenamed: Boolean,
  @JsonProperty("deleted_file")
  val isFileDeleted: Boolean,
  val diff: String,
)

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabGroupDetails(
  val id: Long,
  val name: String,
  val path: String,
  val description: String,
  val fullName: String,
  val fullPath: String,
  val webUrl: String,
)


@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabProject(
  val id: Long,
  val name: String,
  val defaultBranch: String,
  @JsonProperty("archived")
  val isArchived: Boolean,
  val path: String,
  val pathWithNamespace: String,
  val description: String?,
  val webUrl: String,
  // TODO(fredrfli) Use this to avoid crawling already up-to-date projects (Less strain on gitlab)
  val lastActivityAt: ZonedDateTime,
)

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabFile(
  val fileName: String,
  val filePath: String,
  val size: Long,
  val encoding: String,
  val ref: String,
  val commitId: String,
  val lastCommitId: String,
  val content: String,
) {
  companion object {
    private val base64Decoder = Base64.getDecoder()
  }

  val contentBase64Decoded by lazy { String(base64Decoder.decode(content)) }
}

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabFileSummary(
  val id: String,
  val name: String,
  val type: String,
  val path: String,
)

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabNote(
  val id: Long,
  val body: String,
  val author: GitlabUser,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime,
)

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabMilestone(
  val id: Long,
  val iid: Long,
  val title: String,
  val description: String,
  val state: String,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime,
  val closedAt: ZonedDateTime? = null,
  val dueDate: LocalDate? = null,
  val startDate: LocalDate? = null,
  val expired: Boolean = false,
)

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitlabIssue(
  val id: String,
  val iid: Long,
  val title: String,
  val description: String? = "",
  val state: String,
  val projectId: Long,
  val author: GitlabUser,
  val assignees: List<GitlabUser>,
  val labels: Set<String>,
  val upvotes: Int,
  val downvotes: Int,
  val mergeRequestsCount: Int,
  val updatedAt: ZonedDateTime? = null,
  val createdAt: ZonedDateTime,
  val closedAt: ZonedDateTime? = null,
  val closedBy: GitlabUser? = null,
  val milestone: GitlabMilestone? = null,
)
