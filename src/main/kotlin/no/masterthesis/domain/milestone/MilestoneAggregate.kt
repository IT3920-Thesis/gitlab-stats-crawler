package no.masterthesis.domain.milestone

import java.time.ZonedDateTime

data class MilestoneAggregate(
  val groupId: String,
  val projectPath: String,
  val milestoneIid: Long,
  val title: Title,
  val description: Description,
  val createdAt: ZonedDateTime,
  val startDate: ZonedDateTime? = null,
  val dueDate: ZonedDateTime? = null,
  val closedAt: ZonedDateTime? = null,
  val expired: Boolean = false,
) {
  data class Title(
    val length: Int,
    val raw: String,
  )

  data class Description(
    val length: Int = 0,
  )
}
