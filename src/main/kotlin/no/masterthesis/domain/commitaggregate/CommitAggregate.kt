package no.masterthesis.domain.commitaggregate

import java.time.ZonedDateTime

data class CommitAggregate(
  val groupId: String,
  val projectId: String,
  val authorEmail: String,
  val commitSha: String,
  val commitTime: ZonedDateTime = ZonedDateTime.now(),

  val title: Title,
  val size: CommitSize,
  val message: Message? = null,
  val filesChanged: Int,
  val testClassification: TestBalance,
  val isMergeCommit: Boolean,

  val gitLabIssuesReferenced: Set<String> = emptySet(),
) {
  data class Title(
    val length: Int,
    val raw: String,
  )
  data class Message(
    val length: Int,
  )

  enum class CommitSize {
    SMALL,
    MEDIUM,
    LARGE,
  }

  /**
   * Each type is inspired from (Macak 2021).
   *
   * @property PURE_TEST_COMMIT Includes only (or near only) changes to test commits
   * @property MIXED Has both changes to tests and functional code
   * @property NON_TEST_COMMIT Commit contains no test code
   * */
  enum class TestBalance {
    PURE_TEST_COMMIT,
    MIXED,
    NON_TEST_COMMIT
  }
}
