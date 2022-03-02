package no.masterthesis.service.gitlab

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*

@MicronautTest
internal class GitlabCommitCrawlerTest : TestContainersWrapper() {
  @Inject
  private lateinit var commitCrawler: GitlabCommitCrawler

  @Test
  fun `'findAllCommitsByProject' retrieves commit metadata and diffs`() {
    val commits = commitCrawler.findAllCommitsByProject(1021)

    expectThat(commits)
      .isNotEmpty()
      .get { size }.isEqualTo(300)

    expectThat(commits).all {
      get { id }.isNotEmpty()
      get { title }.isNotEmpty()
      get { committer.email }.isNotEmpty()
      get { diffs }.isNotEmpty()
    }
  }
}
