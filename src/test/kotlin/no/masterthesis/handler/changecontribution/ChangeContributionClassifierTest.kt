package no.masterthesis.handler.changecontribution

import java.time.ZonedDateTime
import no.masterthesis.domain.changecontribution.ContributionType
import no.masterthesis.handler.changecontribution.ChangeContributionClassifier.predictContributionType
import no.masterthesis.service.gitlab.GitCommit
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEqualTo

internal class ChangeContributionClassifierTest {

  @Test
  fun `'predictContributionTypes' predicts Java functional code`() {
    val commit = generateCommit(listOf(
      GitlabGitCommitDiff(
        oldPath = "src/main/java/MyApp.java",
        newPath = "src/main/java/MyApp.java",
        aMode = "100644",
        bMode = "100644",
        isNewFile = false,
        isFileRenamed = false,
        isFileDeleted = false,
        diff = "",
      )
    )
    )

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.FUNCTIONAL)
    }
  }

  @Test
  fun `'predictContributionTypes' predicts Java test code`() {
    val commit = generateCommit(listOf(
      GitlabGitCommitDiff(
        oldPath = "src/test/java/MyAppTest.java",
        newPath = "src/test/java/MyAppTest.java",
        aMode = "100644",
        bMode = "100644",
        isNewFile = false,
        isFileRenamed = false,
        isFileDeleted = false,
        diff = "",
      )
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.TEST)
    }
  }

  @Test
  fun `'predictContributionTypes' predicts JavaScript and TypeScript functional code`() {
    val commit = generateCommit(listOf(
      GitlabGitCommitDiff(
        oldPath = "client/src/components/InformationSection/index.js",
        newPath = "client/src/components/InformationSection/index.js",
        aMode = "100644",
        bMode = "100644",
        isNewFile = false,
        isFileRenamed = false,
        isFileDeleted = false,
        diff = "",
      ),
      GitlabGitCommitDiff(
        oldPath = "routes/graphql/searchMedia.js",
        newPath = "routes/graphql/searchMedia.js",
        aMode = "100644",
        bMode = "100644",
        isNewFile = false,
        isFileRenamed = false,
        isFileDeleted = false,
        diff = "",
      ),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.FUNCTIONAL)
    }
  }

  private fun generateCommit(diffs: List<GitlabGitCommitDiff>) = GitCommit(
    id = "123123",
    committer = GitCommit.Author(name = null, "test@example.org"),
    message = null,
    createdAt = ZonedDateTime.now(),
    title = "Add functional tests",
    diffs = diffs,
  )
}
