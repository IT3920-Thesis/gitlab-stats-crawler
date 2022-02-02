package no.masterthesis

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper

@MicronautTest
class GitlabstatscrawlerTest : TestContainersWrapper() {

  @Inject
  lateinit var application: EmbeddedApplication<*>

  @Test
  fun testItWorks() {
    Assertions.assertTrue(application.isRunning)
  }

}
