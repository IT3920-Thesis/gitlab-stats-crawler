package no.masterthesis.service.gitlab

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import no.masterthesis.configuration.TestContainersWrapper
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isNotEmpty
import utils.runBlockingTest

@MicronautTest
class GitlabApiClientTest : TestContainersWrapper() {
  @Inject
  private lateinit var client: GitlabApiClient

  @Test
  fun `throwAway`() = runBlockingTest {
    val projectId = 1021L
    val commits = client
      .findAllCommitsByProject(projectId).awaitSingle().associateBy { it.id }

    val diffs = commits
      .mapValues { client.findCommitDiffs(projectId, commitSha = it.key).awaitSingle() }

    println(diffs)
    expectThat(commits).isNotEmpty()
    expectThat(diffs).isNotEmpty()

    expectThat(diffs.keys).containsExactly(commits.keys)
  }
}
