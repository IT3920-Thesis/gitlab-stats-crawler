package no.masterthesis.handler.projectcrawler

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.gitlabproject.GitlabProject
import no.masterthesis.event.GitlabCommitEvent
import no.masterthesis.factory.RABBITMQ_CRAWL_PROJECT_ID
import no.masterthesis.handler.GitlabCrawlProjectEvent
import org.slf4j.LoggerFactory

@Singleton
@RabbitListener
internal class GitlabProjectCrawlListener(
  @Inject private val summaryCrawler: ProjectSummaryCrawler,
  @Inject private val publisher: ApplicationEventPublisher<GitlabCommitEvent>,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  // numberOfConsumers can be increased to run things more in parallel
  @Queue(RABBITMQ_CRAWL_PROJECT_ID, numberOfConsumers = 1)
  fun onProjectCrawl(data: GitlabCrawlProjectEvent) {
    log.info("Received project to crawl", kv("data", data))

    val summary = summaryCrawler.crawlProject(data.projectId)
    log.info("Project summary", kv("summary", summary))

    val project = GitlabProject(
      groupId = data.subGroupId,
      projectId = data.projectPath,
      codeQualityTools = summary.codeQualityTools,
      illegalFolders = summary.illegalFolders,
    )
  }
}
