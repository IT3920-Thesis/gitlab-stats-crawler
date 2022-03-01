package no.masterthesis.domain.changecontribution

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi

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
    private const val TABLE_NAME = "change_contribution"
  }

  override fun saveAll(contributions: List<ChangeContribution>) {
    TODO("Not yet implemented")
  }
}
