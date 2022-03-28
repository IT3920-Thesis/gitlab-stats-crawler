package no.masterthesis.handler.projectcrawler

import no.masterthesis.handler.projectcrawler.ProjectSummarizer.summarizeFilesInProject
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

internal class ProjectSummarizerTest {

  @Test
  fun `'summarizeFilesInProject' lists node_modules once`() {
    val fileNames = setOf(
      "node_modules",
      "node_modules/@cypress/listr-verbose-renderer/node_modules",
      "node_modules/core-js/library/fn/dom-collections",
      "node_modules/core-js/library/fn/date",
    )

    val summary = summarizeFilesInProject(fileNames)

    expectThat(summary.illegalFolders).containsExactly("node_modules")
  }

  @Test
  fun `'summarizeFilesInProject' lists maven build folders`() {
    val fileNames = setOf(
      "node_modules",
      "node_modules/@cypress/listr-verbose-renderer/node_modules",
      "node_modules/core-js/library/fn/dom-collections",
      "node_modules/core-js/library/fn/date",
    )

    val summary = summarizeFilesInProject(fileNames)

    expectThat(summary.illegalFolders).containsExactly("node_modules")
  }

  @Test
  fun `'summarizeFilesInProject' ignores codeQualityTools inside build folders`() {
    val fileNames = setOf(
      "node_modules/@cypress/listr-verbose-renderer/.eslintrc.json",
      "node_modules/core-js/cypress/cypress.json",
    )

    val summary = summarizeFilesInProject(fileNames)
    expectThat(summary.codeQualityTools).isEmpty()
  }
}
