package no.masterthesis.handler.mergerequest

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.mergerequest.MergeRequestAggregate
import no.masterthesis.domain.mergerequest.MergeRequestAggregateRepository
import no.masterthesis.factory.RABBITMQ_QUEUE_MR_AGGREGATE_ID
import no.masterthesis.handler.GitlabCrawlProjectEvent
import no.masterthesis.service.gitlab.GitlabApiClient
import no.masterthesis.service.gitlab.GitlabMergeRequest
import no.masterthesis.service.gitlab.GitlabNote
import no.masterthesis.util.CommitQualityClassifier
import no.masterthesis.util.asNtnuEmail
import no.masterthesis.util.paginateApiCall
import org.slf4j.LoggerFactory

@Singleton
@RabbitListener
internal class MergeRequestListener(
  @Inject private val client: GitlabApiClient,
  @Inject private val repository: MergeRequestAggregateRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Queue(RABBITMQ_QUEUE_MR_AGGREGATE_ID, numberOfConsumers = 1)
  fun onProject(event: GitlabCrawlProjectEvent) {
    log.info(
      "Received new project to aggregate merge requests from",
      kv("event", event),
    )

    val mergeRequests = runBlocking { paginateApiCall { page ->
      client.listMergeRequests(event.projectId, page = page).awaitSingle()
    } }

    log.info("Found merge requests", kv("mergeRequests", mergeRequests))

    val aggregates = mergeRequests
      .map { mr ->
        mr to listNotesInIssue(projectId = event.projectId, iId = mr.iid)
      }
      .map { (mr, comments) ->

        val issuesReferenced = extractIssueIdsReferenced(mr, comments)

        MergeRequestAggregate(
          groupId = event.subGroupId,
          projectPath = event.projectPath,
          iid = mr.iid,
          title = MergeRequestAggregate.Title(
            raw = mr.title,
            length = mr.title.length,
          ),
          description = MergeRequestAggregate.Description(
            length = mr.description.length,
          ),
          state = mr.state,
          createdAt = mr.createdAt,
          closedAt = mr.closedAt,
          closedBy = mr.closedBy?.username?.asNtnuEmail(),
          mergedAt = mr.mergedAt,
          mergedBy = mr.mergedBy?.username?.asNtnuEmail(),
          updatedAt = mr.updatedAt,
          assignees = mr.assignees.map { it.username.asNtnuEmail() }.toSet(),
          reviewers = mr.reviewers.map { it.username.asNtnuEmail() }.toSet(),
          author = mr.author.username.asNtnuEmail(),
          comments = comments
            .filter { !it.system }
            .map { MergeRequestAggregate.Comment(
              author = it.author.username.asNtnuEmail(),
              createdAt = it.createdAt,
              bodyLength = it.body.length,
            ) },
          issuesReferenced = issuesReferenced,
          milestonesReferenced = emptySet(),
        )
      }

    repository.saveAll(aggregates)
    log.info("Completed aggregating project", kv("aggregates", aggregates.size))
  }

  private fun listNotesInIssue(projectId: Long, iId: Long) = runBlocking {
    client.listNotesInMergeRequest(projectId = projectId, iId = iId).awaitSingle()
  }

  private fun extractIssueIdsReferenced(mr: GitlabMergeRequest, comments: List<GitlabNote>): Set<String> {
    val issuesInComments = comments
      .map { CommitQualityClassifier.extractIssueIds(it.body) }
      .flatten()
      .toSet()

    val issuesReferencedInMetadata = CommitQualityClassifier.extractIssueIds(mr.title)
      .plus(CommitQualityClassifier.extractIssueIds(mr.description))

    return issuesInComments.plus(issuesReferencedInMetadata)
  }
}
