package no.masterthesis.domain.changecontribution

import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory

/**
 * Layer between our PostgreSQL table change_contribution
 * and application code. It converts Data Transition Objects (simple POJOS)
 * to or from SQL.
 * */
@Singleton
internal class PostgreSqlChangeContributionRepository(
  @Inject private val jdbi: Jdbi,
) : ChangeContributionRepository {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private const val TABLE_NAME = "changecontribution"
  }

  override fun saveAll(contributions: List<ChangeContribution>) {
    jdbi.withHandle<Unit, Exception> { handle ->
      val batch = handle.prepareBatch("""
        INSERT INTO $TABLE_NAME (group_id, repository_id, author_email, commit_sha, file_path, type, timestamp, lines_added, lines_removed, previous_file_path) 
        VALUES (:group_id, :repository_id, :author_email, :commit_sha, :file_path, :type, :timestamp, :lines_added, :lines_removed, :previous_file_path)
        ON CONFLICT (group_id, repository_id, author_email, commit_sha, file_path) DO UPDATE
        SET type=:type, timestamp=:timestamp, lines_added=:lines_added, lines_removed=:lines_removed, previous_file_path=:previous_file_path
      """.trimIndent())

      contributions.forEach {
        batch
          .bind("group_id", it.groupId)
          .bind("repository_id", it.repositoryId)
          .bind("author_email", it.authorEmail)
          .bind("commit_sha", it.commitSha)
          .bind("file_path", it.filePath)
          .bind("type", it.type)
          .bind("timestamp", it.createdOn)
          .bind("lines_added", it.linesAdded)
          .bind("lines_removed", it.linesRemoved)
          .bind("previous_file_path", it.previousFilePath)
          .add()
      }

      val rowsChanged = batch.execute()
      log.info("Successfully executed query", kv("rowsChanged", rowsChanged))
    }
  }
}
