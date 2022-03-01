package no.masterthesis.domain.changecontribution

interface ChangeContributionRepository {
  fun saveAll(contributions: List<ChangeContribution>)
}
