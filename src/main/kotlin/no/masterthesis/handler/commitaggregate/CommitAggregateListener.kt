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
import no.masterthesis.util.CommitQualityClassifier.isMergeCommit
import no.masterthesis.service.gitlab.GitlabCommitCrawler
import no.masterthesis.service.gitlab.GitlabFileCrawler
import no.masterthesis.util.CommitQualityClassifier
import no.masterthesis.util.MailMapUtil
import org.slf4j.LoggerFactory

@Singleton
@RabbitListener
internal class CommitAggregateListener(
  @Inject private val commitCrawler: GitlabCommitCrawler,
  @Inject private val repository: CommitAggregateRepository,
  @Inject private val fileCrawler: GitlabFileCrawler,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Queue(RABBITMQ_QUEUE_COMMIT_AGGREGATE_ID, numberOfConsumers = 1)
  fun onProject(event: GitlabCrawlProjectEvent) {
    log.info(
      "Received new project to classify commits from",
      kv("event", event),
    )

    val commits = commitCrawler.findAllCommitsByProject(event.projectId)
    log.trace("Commits retrieved", kv("projectId", event.projectId), kv("commits", commits.size))

    val aggregates = commits.map { commit ->
      CommitAggregate(
        groupId = event.subGroupId,
        projectId = event.projectPath,
        authorEmail = overrideCommitEmails(
          projectId = event.projectId,
          branch = event.defaultBranch,
          email = commit.committer.email,
        ),
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
        isMergeCommit = isMergeCommit(commit),
      ).also {
        log.trace("Constructed aggregate for commit", kv("commitSha", commit.id), kv("aggregate", it))
      }
    }

    repository.saveAll(aggregates)
  }

  private fun overrideCommitEmails(projectId: Long, branch: String, email: String): String {
    val mailMap = fileCrawler.retrieveMailMap(projectId = projectId.toString(), branch)

    return MailMapUtil.overrideCommitEmails(mailMap, email)
  }
}
