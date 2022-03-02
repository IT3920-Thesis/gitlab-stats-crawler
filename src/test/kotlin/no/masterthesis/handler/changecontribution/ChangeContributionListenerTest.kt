package no.masterthesis.handler.changecontribution

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.masterthesis.configuration.TestContainersWrapper
import no.masterthesis.event.GitlabCommitEvent
import no.masterthesis.service.gitlab.GitlabCommitCrawler
import org.junit.jupiter.api.Test

@MicronautTest
internal class ChangeContributionListenerTest : TestContainersWrapper() {
  @Inject
  private lateinit var changeContributionListener: ChangeContributionListener

  @Inject
  private lateinit var commitCrawler: GitlabCommitCrawler

  @Test
  fun `Classifies all contributions from a gitlab repository`() {
    val events = commitCrawler.findAllCommitsByProject(1021).takeLast(3)

    events.forEach {
      changeContributionListener.onCommit(GitlabCommitEvent(
        groupId = "prosjekt4",
        repositoryId = "gruppe20",
        commit = it,
      ))
    }
  }
}
