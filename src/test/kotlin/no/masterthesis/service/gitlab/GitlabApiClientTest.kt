package no.masterthesis.service.gitlab

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import strikt.api.expectThat
import strikt.assertions.isEmpty

@MicronautTest
class GitlabApiClientTest : TestContainersWrapper() {
  @Inject
  private lateinit var client: GitlabApiClient

  @Test
  fun `throwAway`() {
    val projectId = 1021L
    val results = Mono.from(client.findCommitsByProject(projectId)).block()

    expectThat(results).isEmpty()
  }
}
