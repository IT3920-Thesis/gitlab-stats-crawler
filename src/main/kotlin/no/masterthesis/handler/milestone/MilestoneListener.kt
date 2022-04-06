package no.masterthesis.handler.milestone

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.ZoneOffset
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import net.logstash.logback.argument.StructuredArguments.raw
import no.masterthesis.domain.gitlabissue.IssueAggregateRepository
import no.masterthesis.domain.milestone.MilestoneAggregate
import no.masterthesis.domain.milestone.ProjectMilestoneAggregateRepository
import no.masterthesis.factory.RABBITMQ_QUEUE_MILESTONE_AGGREGATE_ID
import no.masterthesis.handler.GitlabCrawlProjectEvent
import no.masterthesis.service.gitlab.GitlabApiClient
import org.slf4j.LoggerFactory

@Singleton
@RabbitListener
internal class MilestoneListener(
  @Inject private val client: GitlabApiClient,
  @Inject private val repository: ProjectMilestoneAggregateRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Queue(RABBITMQ_QUEUE_MILESTONE_AGGREGATE_ID, numberOfConsumers = 1)
  fun onProject(event: GitlabCrawlProjectEvent) {
    log.info(
      "Received new project to aggregate milestones from",
      kv("event", event),
    )

    val milestones = runBlocking { client.listProjectMilestones(projectId = event.projectId).awaitSingle() }

    val aggregates = milestones.map { milestone ->
      MilestoneAggregate(
        groupId = event.subGroupId,
        projectPath = event.projectPath,
        milestoneIid = milestone.iid,
        title = MilestoneAggregate.Title(
          raw = milestone.title,
          length = milestone.title.length,
        ),
        description = MilestoneAggregate.Description(
          length = milestone.description.length,
        ),
        createdAt = milestone.createdAt,
        closedAt = milestone.closedAt,
        startDate = milestone.startDate?.atStartOfDay(ZoneOffset.UTC),
        dueDate = milestone.dueDate?.atStartOfDay(ZoneOffset.UTC),
        expired = milestone.expired,
      )
    }

    log.info("Saving aggregated milestones to db...", kv("milestones", aggregates.size), kv("projectId", event.projectId))
    repository.saveAll(aggregates)
  }
}
