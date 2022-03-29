package no.masterthesis.domain.commitaggregate

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.masterthesis.domain.changecontribution.PostgreSqlChangeContributionRepository
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory

@Singleton
internal class PostgreSqlCommitAggregateRepository(
  @Inject private val jdbi: Jdbi,
  @Inject private val objectMapper: ObjectMapper,
) : CommitAggregateRepository {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private const val TABLE_NAME = "commitaggregate"
  }

  override fun saveAll(commits: List<CommitAggregate>) {
    jdbi.withHandle<Unit, Exception> { handle ->
      @Suppress("MaxLineLength")
      val batch = handle.prepareBatch("""
        INSERT INTO $TABLE_NAME (group_id, project_path, commit_sha, author_email, commit_time, size, title, message, files_changed, test_classification, gitlab_issues_referenced) 
        VALUES (:group_id, :project_path, :commit_sha, :author_email, :commit_time, :size, :title, :message, :files_changed, :test_classification, :gitlab_issues_referenced)
        ON CONFLICT (group_id, project_path, commit_sha, author_email, commit_time) DO UPDATE
        SET size=:size, title=:title, message=:message, files_changed=:files_changed, test_classification=:test_classification, gitlab_issues_referenced=:gitlab_issues_referenced
      """.trimIndent())

      commits.forEach {
        batch
          .bind("group_id", it.groupId)
          .bind("project_path", it.projectId)
          .bind("author_email", it.authorEmail)
          .bind("commit_sha", it.commitSha)
          .bind("commit_time", it.commitTime)
          .bind("size", it.size)
          .bind("title", objectMapper.writeValueAsString(it.title))
          .bind("message", objectMapper.writeValueAsString(it.message))
          .bind("files_changed", it.filesChanged)
          .bind("test_classification", it.testClassification)
          .bind("gitlab_issues_referenced", objectMapper.writeValueAsString(it.gitLabIssuesReferenced))
          .add()
      }

      batch.execute()
      log.trace("Successfully executed query")
    }
  }
}
