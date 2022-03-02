package no.masterthesis.domain.changecontribution

interface ChangeContributionRepository {
  /**
   * Saves a list of contributions into the database at the same time.
   * Note saving is idempotent, meaning that duplicates are overwritten by the new values.
   * */
  fun saveAll(contributions: List<ChangeContribution>)
}
