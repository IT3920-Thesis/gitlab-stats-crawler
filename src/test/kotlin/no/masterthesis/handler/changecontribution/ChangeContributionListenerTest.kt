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
    val events = commitCrawler.findAllCommitsByProject(15463).takeLast(3)

    events.forEach {
      changeContributionListener.onCommit(GitlabCommitEvent(
        rootGroupId = "it3920-gitlab-projects-examples",
        groupId = "group2022001",
        repositoryPath = "randominternalproject002",
        projectId = 15463,
        defaultBranch = "master",
        commit = it,
      ))
    }
  }
}
