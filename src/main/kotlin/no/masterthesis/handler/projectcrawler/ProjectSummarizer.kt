package no.masterthesis.handler.projectcrawler

import no.masterthesis.domain.projectsummary.CodeQualityTool
import no.masterthesis.util.CodeQualityParser
import no.masterthesis.util.FileClassifierUtil
import no.masterthesis.util.FileClassifierUtil.buildFolders
import no.masterthesis.util.FileClassifierUtil.dataFiles
import org.slf4j.LoggerFactory

/**
 * Extracts summaries from a project, such as what type different files are.
 * */
internal object ProjectSummarizer {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun summarizeFilesInProject(fileNames: Set<String>): ProjectCrawlResults {
    val illegalFolders = listIllegalFolders(fileNames)

    val linterFiles = fileNames
      // Contents inside build folders are ignored
      .filter { !FileClassifierUtil.isInBuildFolder(it) }
      .filter { CodeQualityParser.isLinterConfig(it) }
      .map { CodeQualityTool(name = it, type = CodeQualityTool.CodeQualityToolType.LINTER) }

    val testTools = fileNames
      // Contents inside build folders are ignored
      .filter { !FileClassifierUtil.isInBuildFolder(it) }
      .filter { it.contains("jest.config.") || it.contains("cypress/") }
      .map { CodeQualityTool(name = it, type = CodeQualityTool.CodeQualityToolType.TEST) }

    val codeQualityTools = linterFiles.plus(testTools)

    return ProjectCrawlResults(
      codeQualityTools = codeQualityTools,
      illegalFolders = illegalFolders,
    )
  }

  private fun listIllegalFolders(fileNames: Set<String>): Set<String> {
    val illegalFolders = fileNames
      .flatMap { fileName ->
        buildFolders.map { illegalFolder ->
          if (fileName.contains(illegalFolder)) illegalFolder else null
        }
      }
      .filterNotNull()

    val illegalFiles = fileNames
      .flatMap { fileName ->
        dataFiles.map { file ->
          if (fileName.contains(file)) file else null
        }
      }
      .filterNotNull()

    return illegalFolders.plus(illegalFiles).toSet()
  }
}
