package no.masterthesis.domain.commitaggregate

interface CommitAggregateRepository {
  fun saveAll(commits: List<CommitAggregate>)
}
