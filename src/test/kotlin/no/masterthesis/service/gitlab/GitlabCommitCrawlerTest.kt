package no.masterthesis.service.gitlab

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull

@MicronautTest
internal class GitlabCommitCrawlerTest : TestContainersWrapper() {
  @Inject
  private lateinit var commitCrawler: GitlabCommitCrawler

  @Test
  fun `'findAllCommitsByProject' retrieves commit metadata and diffs`() {
    val commits = commitCrawler.findAllCommitsByProject(1021)

    expectThat(commits).isNotEmpty()

    println("\n\nCommit")
    println(commits.find { it.id == "f5d3c45ceb0a8e0ed026f3c589b8a912c4d9ffee" })

    expectThat(commits).all {
      get { id }.isNotEmpty()
      get { title }.isNotEmpty()
      get { committer.email }.isNotEmpty()
      get { diffs }.isNotEmpty()
    }
  }
}
