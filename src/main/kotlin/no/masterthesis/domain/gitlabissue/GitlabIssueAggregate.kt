package no.masterthesis.domain.gitlabissue

import java.time.ZonedDateTime

/**
 * Aggregated representation of issues in gitlab
 * per project. Each entry is an individual issue.
 *
 * @property groupId Path of the subgroup this project is located inside
 * @property projectPath
 * @property issueIid The issue id, relative to a project (iid is used in urls and in references from commits and MRs)
 * @property title
 * @property description Contains currently only the length of a description.
 * @property createdAt Time the issue was created
 * @property author The person who created the issue
 * @property closedAt Time the issue were closed
 * @property closedBy Who closed the issue
 * @property state Flags whether the issue is open or closed
 * @property labels Set of labels referenced in this issue
 * @property assignees People who have been assigned to the issue
 * @property relatedIssues Issue Ids that references this issue
 * @property notes List of notes i this issue (Records only who wrote the note and the length of the note)
 * */
data class GitlabIssueAggregate(
  val groupId: String,
  val projectPath: String,
  val issueIid: Long,
  val title: Title,
  val description: Description,
  val createdAt: ZonedDateTime,
  val author: String,
  val closedAt: ZonedDateTime?,
  val closedBy: String?,
  val state: State,
  val labels: Set<String>,
  val assignees: Set<String>,
  val relatedIssues: Set<String>,
  val notes: List<Note>,
) {
  data class Title(
    val length: Int,
    val raw: String,
  )

  data class Description(
    val length: Int = 0,
  )

  enum class State { OPEN, CLOSED }

  data class Note(
    val bodyLength: Long,
    val author: String,
    val createdAt: ZonedDateTime,
  )
}
