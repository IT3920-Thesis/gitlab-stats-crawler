package no.masterthesis.domain.mergerequest

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.masterthesis.domain.gitlabissue.GitlabIssueAggregate
import no.masterthesis.domain.gitlabissue.PostgreSqlIssueAggregateRepository
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory

@Singleton
internal class PostgreSqlMergeRequestAggregateRepository(
  @Inject private val jdbi: Jdbi,
  @Inject private val objectMapper: ObjectMapper,
) : MergeRequestAggregateRepository {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val TABLE_NAME = "mergerequestaggregate"
  }

  override fun saveAll(items: List<MergeRequestAggregate>) {
    val keyFields = listOf("group_id", "project_path", "iid")
    val valueFields = listOf(
      "title",
      "description",
      "author",
      "state",
      "created_at",
      "closed_at",
      "closed_by",
      "merged_at",
      "merged_by",
      "updated_at",
      "assignees",
      "reviewers",
      "milestones_referenced",
      "issues_referenced",
      "comments",
    )
    jdbi.withHandle<Unit, Exception> { handle ->
      @Suppress("MaxLineLength")
      val batch = handle.prepareBatch("""
        INSERT INTO $TABLE_NAME (${keyFields.joinToString(", ")}, ${valueFields.joinToString(", ")}) 
        VALUES (${keyFields.joinToString(", ") { ":$it" }}, ${valueFields.joinToString(", ") { ":$it" }})
        ON CONFLICT (${keyFields.joinToString(", ")}) DO UPDATE
        SET ${valueFields.joinToString(", ") { "$it=:$it" }}
      """.trimIndent())

      items.forEach {
        batch
          .bind("group_id", it.groupId)
          .bind("project_path", it.projectPath)
          .bind("iid", it.iid)
          .bind("title", objectMapper.writeValueAsString(it.title))
          .bind("description", objectMapper.writeValueAsString(it.description))
          .bind("created_at", it.createdAt)
          .bind("author", it.author)
          .bind("updated_at", it.updatedAt)
          .bind("closed_at", it.closedAt)
          .bind("closed_by", it.closedBy)
          .bind("merged_at", it.mergedAt)
          .bind("merged_by", it.mergedBy)
          .bind("state", it.state)
          .bind("assignees", objectMapper.writeValueAsString(it.assignees))
          .bind("reviewers", objectMapper.writeValueAsString(it.reviewers))
          .bind("milestones_referenced", objectMapper.writeValueAsString(it.milestonesReferenced))
          .bind("issues_referenced", objectMapper.writeValueAsString(it.issuesReferenced))
          .bind("comments", objectMapper.writeValueAsString(it.comments))
          .add()
      }

      batch.execute()
      log.trace("Successfully executed query")
    }
  }
}
