package no.masterthesis.service.gitlab

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isNotEmpty

@MicronautTest
internal class GitlabFileCrawlerTest : TestContainersWrapper() {
  @Inject
  private lateinit var fileCrawler: GitlabFileCrawler

  @Test
  fun `'retrieveMailMap' retrieves the parsed mailmap`() {
    val mailMap = fileCrawler.retrieveMailMap("15463", "master")
    expectThat(mailMap).isNotEmpty()
  }

  @Test
  fun `'retrieveMailMap' returns empty map if project has no mailmap`() {
    val mailMap = fileCrawler.retrieveMailMap("15450", "master")
    println(mailMap)
    expectThat(mailMap).isEmpty()
  }
}
