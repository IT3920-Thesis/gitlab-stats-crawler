package no.masterthesis.handler.projectcrawler

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize

@MicronautTest
internal class ProjectSummaryCrawlerTest : TestContainersWrapper() {
  @Inject
  private lateinit var projectCrawler: ProjectSummaryCrawler

  @Test
  fun `'crawlProject'`() {
    val summary = projectCrawler.crawlProject(15903)
    expectThat(summary.illegalFolders).hasSize(2)
  }
}
