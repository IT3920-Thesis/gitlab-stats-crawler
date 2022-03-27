package no.masterthesis.handler

import io.micronaut.rabbitmq.annotation.Binding
import io.micronaut.rabbitmq.annotation.RabbitClient
import no.masterthesis.factory.RABBITMQ_FANOUT_PROJECT_CRAWLED

internal data class GitlabCrawlProjectEvent(
  val rootGroupId: String,
  val subGroupId: String,
  val projectPath: String,
  val defaultBranch: String,
  val projectId: Long,
)

@RabbitClient(RABBITMQ_FANOUT_PROJECT_CRAWLED)
internal interface GitlabProjectClient {
  @Binding
  fun publish(project: GitlabCrawlProjectEvent)
}
