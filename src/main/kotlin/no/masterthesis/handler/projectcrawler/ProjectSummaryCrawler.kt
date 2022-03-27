package no.masterthesis.handler.projectcrawler

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.gitlabproject.CodeQualityTool
import no.masterthesis.domain.gitlabproject.GitlabProject
import no.masterthesis.service.gitlab.GitlabApiClient
import no.masterthesis.util.CodeQualityParser
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

  companion object {
    private val buildFolders = setOf(
      "target",
      ".gradle",
      "build",
      "node_modules",
      "dist",
      "venv/",
    )

    private val dataFiles = setOf(
      "db.sqlite3",
      "db.sqlite3-journal",
    )
  }

  fun crawlProject(projectId: Long): ProjectCrawlResults {
    val files = listAllFilesInProject(projectId.toString())

    val illegalFolders = files.filter { keepBuildFiles(it) || keepDataFiles(it) }.toSet()

    val linterFiles = files
      .filter { CodeQualityParser.isLinterConfig(it) }
      .map { CodeQualityTool(name = it, type = CodeQualityTool.CodeQualityToolType.LINTER) }
    val testTools = files
      .filter { it.contains("jest.config.") || it.contains("cypress/") }
      .map { CodeQualityTool(name = it, type = CodeQualityTool.CodeQualityToolType.TEST) }

    return ProjectCrawlResults(
      illegalFolders = illegalFolders,
      codeQualityTools = linterFiles.plus(testTools),
    )
  }

  private fun listAllFilesInProject(projectId: String): Set<String> {
    val files = runBlocking {
      paginateApiCall { page -> client.listFilesInRepository(projectId, page = page).awaitSingle() }
    }

    log.trace("All files in repository has been retrieved", kv("files", files))
    return files.map { it.path }.toSet()
  }

  private fun keepBuildFiles(path: String): Boolean {
    return buildFolders.any { path.contains(it) }
  }

  private fun keepDataFiles(path: String) = dataFiles.any { path.contains(it) }
}
