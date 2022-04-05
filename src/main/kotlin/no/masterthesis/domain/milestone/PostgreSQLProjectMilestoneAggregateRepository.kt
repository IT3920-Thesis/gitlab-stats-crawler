package no.masterthesis.domain.milestone

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory

@Singleton
internal class PostgreSQLProjectMilestoneAggregateRepository(
  @Inject private val jdbi: Jdbi,
  @Inject private val objectMapper: ObjectMapper,
) : ProjectMilestoneAggregateRepository {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private const val TABLE_NAME = "projectmilestoneaggregate"
  }

  override fun saveAll(items: List<MilestoneAggregate>) {
    jdbi.withHandle<Unit, Exception> { handle ->
      @Suppress("MaxLineLength")
      val batch = handle.prepareBatch("""
        INSERT INTO $TABLE_NAME (group_id, project_path, milestone_iid, title, description, created_at, closed_at, start_date, due_date, expired) 
        VALUES (:group_id, :project_path, :milestone_iid, :title, :description, :created_at, :closed_at, :start_date, :due_date, :expired)
        ON CONFLICT (group_id, project_path, milestone_iid) DO UPDATE
        SET title=:title, description=:description, created_at=:created_at, closed_at=:closed_at, start_date=:start_date, due_date=:due_date, expired=:expired
      """.trimIndent())

      items.forEach {
        batch
          .bind("group_id", it.groupId)
          .bind("project_path", it.projectPath)
          .bind("milestone_iid", it.milestoneIid)
          .bind("title", objectMapper.writeValueAsString(it.title))
          .bind("description", objectMapper.writeValueAsString(it.description))
          .bind("created_at", it.createdAt)
          .bind("closed_at", it.closedAt)
          .bind("start_date", it.startDate)
          .bind("due_date", it.dueDate)
          .bind("expired", it.expired)
          .add()
      }

      batch.execute()
      log.trace("Successfully executed query")
    }
  }
}
