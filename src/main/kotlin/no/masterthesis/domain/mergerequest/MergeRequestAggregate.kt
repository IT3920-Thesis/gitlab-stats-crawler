package no.masterthesis.domain.mergerequest

import java.time.ZonedDateTime
import no.masterthesis.service.gitlab.GitlabMergeRequest

data class MergeRequestAggregate(
  val groupId: String,
  val projectPath: String,
  val iid: Long,
  val title: Title,
  val description: Description,
  val state: GitlabMergeRequest.State,
  val createdAt: ZonedDateTime,
  val author: String,
  val closedAt: ZonedDateTime?,
  val closedBy: String?,
  val mergedAt: ZonedDateTime?,
  val updatedAt: ZonedDateTime?,
  val mergedBy: String?,
  val assignees: Set<String>,
  val reviewers: Set<String>,
  val milestonesReferenced: Set<String>,
  val issuesReferenced: Set<String>,
  val comments: List<Comment>,
) {
  data class Title(
    val raw: String,
    val length: Int,
  )

  data class Description(
    val length: Int,
  )

  data class Comment(
    val author: String,
    val createdAt: ZonedDateTime,
    val bodyLength: Int,
  )
}
