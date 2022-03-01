package no.masterthesis.domain.changecontribution

import java.time.ZonedDateTime

enum class ContributionType {
  FUNCTIONAL,
  TEST,
  DOCUMENTATION,
  OTHER,
}

data class ChangeContribution(
  /**
   * Key used to determine who has access to a project
   * */
  val groupId: String,
  /**
   * Key used determine who contributed to this change
   * */
  val authorEmail: String,
  /**
   * Checksum for the Git commit
   * */
  val commitSha: String,
  /**
   * Key used to identify the filepath to the contribution.
   * */
  val filePath: String,
  val type: ContributionType,
  val createdOn: ZonedDateTime,
  /**
   * The number of lines added for the specified commit and filePath.
   * It should adjust for renames/file moves.
   * */
  val linesAdded: Long,
  /**
   * Similar to [linesAdded] but for lines removed.
   * */
  val linesRemoved: Long,

  /**
   * Flag used to tell what the previous filePath was,
   * used to detect renames or moved files.
   * */
  val previousFilePath: String? = null,
)
