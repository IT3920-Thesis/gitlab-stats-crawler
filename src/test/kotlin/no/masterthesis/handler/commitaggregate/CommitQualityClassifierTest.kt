package no.masterthesis.handler.commitaggregate

import java.time.ZonedDateTime
import no.masterthesis.util.CommitQualityClassifier.extractIssueIdsReferenced
import no.masterthesis.service.gitlab.GitCommit
import no.masterthesis.service.gitlab.GitlabGitCommit
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import strikt.api.expectThat
import strikt.assertions.containsExactly

internal class CommitQualityClassifierTest {
  @ParameterizedTest
  @CsvSource(value = [
    "#31 commit title,#31",
    "Commit title #20,#20",
  ])
  fun `'extractIssueIdsReferenced' extracts issues from title`(raw: String, expectedIssue: String) {
    val extractedIssues = extractIssueIdsReferenced(GitCommit(
      title = raw,
      committer = GitCommit.Author("", ""),
      createdAt = ZonedDateTime.now(),
      diffs = emptyList(),
      id = "00b458f0736cd5321ed419049476797bb2626f2a",
      message = null,
      stats = GitlabGitCommit.Stats(
        additions = 0,
        deletions = 0,
        total = 0,
      ),
    ))

    expectThat(extractedIssues).containsExactly(expectedIssue)
  }

  @Test
  fun `'extractIssueIdsReferenced' extracts issues from multiline message`() {
    val extractedIssues = extractIssueIdsReferenced(GitCommit(
      title = "",
      committer = GitCommit.Author("", ""),
      createdAt = ZonedDateTime.now(),
      diffs = emptyList(),
      id = "00b458f0736cd5321ed419049476797bb2626f2a",
      message = "this is a message 'feat/api'\n\n#2",
      stats = GitlabGitCommit.Stats(
        additions = 0,
        deletions = 0,
        total = 0,
      ),
    ))

    expectThat(extractedIssues).containsExactly("#2")
  }
}
