package no.masterthesis.handler.commitaggregate

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.commitaggregate.CommitAggregate
import no.masterthesis.domain.commitaggregate.CommitAggregateRepository
import no.masterthesis.factory.RABBITMQ_QUEUE_COMMIT_AGGREGATE_ID
import no.masterthesis.handler.GitlabCrawlProjectEvent
import no.masterthesis.service.gitlab.GitlabCommitCrawler
import org.slf4j.LoggerFactory

@Singleton
@RabbitListener
internal class CommitAggregateListener(
  @Inject private val commitCrawler: GitlabCommitCrawler,
  @Inject private val repository: CommitAggregateRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Queue(RABBITMQ_QUEUE_COMMIT_AGGREGATE_ID, numberOfConsumers = 1)
  fun onProject(event: GitlabCrawlProjectEvent) {
    log.info(
      "Received new project to classify commits from",
      kv("event", event),
    )

    val commits = commitCrawler.findAllCommitsByProject(event.projectId)
    log.info("Commits retrieved", kv("projectId", event.projectId), kv("commits", commits.size))

    val aggregates = commits.map { commit ->
      CommitAggregate(
        groupId = event.subGroupId,
        projectId = event.projectPath,
        authorEmail = commit.committer.email,
        commitSha = commit.id,
        commitTime = commit.createdAt,
        title = CommitAggregate.Title(
          length = commit.title.length,
          raw = commit.title,
        ),
        message = commit.message?.let { CommitAggregate.Message(
          length = it.length
        ) },
        filesChanged = commit.diffs.size,
        gitLabIssuesReferenced = CommitQualityClassifier.extractIssueIdsReferenced(commit),
        size = CommitQualityClassifier.classifyCommitSize(commit),
        testClassification = CommitQualityClassifier.classifyCommitTestBalance(commit),
      ).also {
        log.info("Constructed aggregate for commit", kv("commitSha", commit.id), kv("aggregate", it))
      }
    }

    repository.saveAll(aggregates)
  }
}
