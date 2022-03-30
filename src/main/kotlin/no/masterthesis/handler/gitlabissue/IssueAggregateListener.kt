package no.masterthesis.handler.gitlabissue

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import net.logstash.logback.argument.StructuredArguments.raw
import no.masterthesis.domain.gitlabissue.GitlabIssueAggregate
import no.masterthesis.domain.gitlabissue.IssueAggregateRepository
import no.masterthesis.factory.RABBITMQ_QUEUE_ISSUE_AGGREGATE_ID
import no.masterthesis.handler.GitlabCrawlProjectEvent
import no.masterthesis.service.gitlab.GitlabApiClient
import no.masterthesis.util.asNtnuEmail
import no.masterthesis.util.paginateApiCall
import org.slf4j.LoggerFactory

@Singleton
@RabbitListener
internal class IssueAggregateListener(
  @Inject private val client: GitlabApiClient,
  @Inject private val repository: IssueAggregateRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Queue(RABBITMQ_QUEUE_ISSUE_AGGREGATE_ID, numberOfConsumers = 1)
  fun onProject(event: GitlabCrawlProjectEvent) {
    log.info(
      "Received new project to aggregate issues from",
      kv("event", event),
    )

    val issues = listAllIssues(event.projectId)
      .map { it to listNotesInIssue(event.projectId, it.iid) }

    val aggregates = issues.map { (issue, notes) ->
      GitlabIssueAggregate(
        groupId = event.subGroupId,
        projectPath = event.projectPath,
        issueIid = issue.iid,
        title = GitlabIssueAggregate.Title(
          length = issue.title.length,
          raw = issue.title,
        ),
        description = GitlabIssueAggregate.Description(
          length = issue.description?.length ?: 0,
        ),
        state = when {
          issue.state.lowercase() == "closed" -> GitlabIssueAggregate.State.CLOSED
          else -> GitlabIssueAggregate.State.OPEN
        },
        createdAt = issue.createdAt,
        // We assume the user's username is the prefix for their NTNU account eduPersonPrincipalName
        author = issue.author.username.asNtnuEmail(),
        closedAt = issue.closedAt,
        closedBy = issue.closedBy?.username?.asNtnuEmail(),
        labels = issue.labels,
        assignees = issue.assignees.map { it.username.asNtnuEmail() }.toSet(),
        notes = notes.map {
          GitlabIssueAggregate.Note(
            author = it.author.username.asNtnuEmail(),
            createdAt = it.createdAt,
            bodyLength = it.body.length.toLong(),
          )
        },
        relatedIssues = emptySet(),
      )
    }

    repository.saveAll(aggregates)
  }

  private fun listAllIssues(projectId: Long) = runBlocking {
    paginateApiCall { page ->
      client.listIssuesInProject(projectId = projectId, page = page).awaitSingle()
    }
  }

  private fun listNotesInIssue(projectId: Long, issueIid: Long) = runBlocking {
    client.listNotesInIssue(projectId = projectId, issueIid = issueIid).awaitSingle()
  }
}
