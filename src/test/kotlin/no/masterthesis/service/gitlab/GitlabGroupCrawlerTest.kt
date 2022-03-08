package no.masterthesis.service.gitlab

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import utils.runBlockingTest

@MicronautTest
internal class GitlabGroupCrawlerTest : TestContainersWrapper() {
  @Inject
  private lateinit var groupCrawler: GitlabGroupCrawler

  /**
   * We are limited to one subgroup level. So projects inside a subgroup's subgroup
   * are not found
   * */
  @Test
  fun `'crawlGitlabGroup' lists all projects inside the group and subgroup`() = runBlockingTest {
    val groupPath = "it3920-gitlab-projects-examples"
    val groups = groupCrawler.crawlGitlabGroup(groupPath)

    expectThat(groups).isNotEmpty()
    expectThat(groups).all {
      get { projects }.isNotEmpty()
      get { groupId }.isNotEmpty()
      get { baseGroupPath }.isEqualTo(groupPath)
    }
  }
}
