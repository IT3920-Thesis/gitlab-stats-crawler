package no.masterthesis.handler.projectcrawler

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.projectsummary.CodeQualityTool
import no.masterthesis.service.gitlab.GitlabApiClient
import no.masterthesis.util.paginateApiCall
import org.slf4j.LoggerFactory

internal data class ProjectCrawlResults(
  val codeQualityTools: List<CodeQualityTool> = emptyList(),
  val illegalFolders: Set<String> = emptySet(),
)

@Singleton
internal class ProjectSummaryCrawler(
  @Inject private val client: GitlabApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun crawlProject(projectId: Long): ProjectCrawlResults {
    val files = listAllFilesInProject(projectId.toString())

    return ProjectSummarizer.summarizeFilesInProject(files)
  }

  private fun listAllFilesInProject(projectId: String): Set<String> {
    val files = runBlocking {
      paginateApiCall { page -> client.listFilesInRepository(projectId, page = page).awaitSingle() }
    }

    log.trace("All files in repository has been retrieved", kv("files", files))
    return files.map { it.path }.toSet()
  }
}
