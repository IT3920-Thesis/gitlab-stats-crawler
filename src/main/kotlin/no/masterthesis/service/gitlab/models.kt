package no.masterthesis.service.gitlab

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import java.time.ZonedDateTime

/**
 * This file contains a mapping of Gitlab's API response structures,
 * into deserializable data classes.
 * */

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
  // We might need to add some additional packages to Jackson
  val authoredDate: ZonedDateTime,
  val committerName: String?,
  val committerEmail: String,
  val committedDate: ZonedDateTime,
  val createdAt: ZonedDateTime,
  val parentIds: List<String>,
  val webUrl: String?,
)

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
