package no.masterthesis.domain.gitlabissue

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory

@Singleton
internal class PostgreSqlIssueAggregateRepository(
  @Inject private val jdbi: Jdbi,
  @Inject private val objectMapper: ObjectMapper,
) : IssueAggregateRepository {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private const val TABLE_NAME = "issueaggregate"
  }

  override fun saveAll(items: List<GitlabIssueAggregate>) {
    jdbi.withHandle<Unit, Exception> { handle ->
      @Suppress("MaxLineLength")
      val batch = handle.prepareBatch("""
        INSERT INTO $TABLE_NAME (group_id, project_path, issue_iid, title, description, created_at, author, closed_at, closed_by, state, labels, assignees, notes) 
        VALUES (:group_id, :project_path, :issue_iid, :title, :description, :created_at, :author, :closed_at, :closed_by, :state, :labels, :assignees, :notes)
        ON CONFLICT (group_id, project_path, issue_iid) DO UPDATE
        SET title=:title, description=:description, created_at=:created_at, author=:author, closed_at=:closed_at, closed_by=:closed_by, state=:state, labels=:labels, assignees=:assignees, notes=:notes
      """.trimIndent())

      items.forEach {
        batch
          .bind("group_id", it.groupId)
          .bind("project_path", it.projectPath)
          .bind("issue_iid", it.issueIid)
          .bind("title", objectMapper.writeValueAsString(it.title))
          .bind("description", objectMapper.writeValueAsString(it.description))
          .bind("created_at", it.createdAt)
          .bind("author", it.author)
          .bind("closed_at", it.closedAt)
          .bind("closed_by", it.closedBy)
          .bind("state", it.state)
          .bind("labels", objectMapper.writeValueAsString(it.labels))
          .bind("assignees", objectMapper.writeValueAsString(it.assignees))
          .bind("notes", objectMapper.writeValueAsString(it.notes))
          .add()
      }

      batch.execute()
      log.trace("Successfully executed query")
    }
  }
}
