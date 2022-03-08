package no.masterthesis.domain.changecontribution

import java.time.ZonedDateTime

enum class ContributionType {
  /**
   * Code that provides business value
   * */
  FUNCTIONAL,
  /**
   * Code related to testing
   * */
  TEST,
  /**
   * Files that are known to cover documentation,
   * such as Markdown files
   * */
  DOCUMENTATION,
  /**
   * Fairly broad category. It covers files that are assumed
   * to be used to configure the project.
   * This could be for infrastructure (Dockerfile, nginx.conf) or general project config (pom.xml, .eslintrc, *.yml).
   * */
  CONFIGURATION,
  /**
   * Dumping ground for everything else that we couldn't classify.
   * */
  OTHER,
}

data class ChangeContribution(
  /**
   * Key used to determine who has access to a project
   * */
  val groupId: String,
  /**
   * Denotes the URL-safe name of the repository. Should uniquelly identify the repository inside a group (or from the global level)
   * */
  val repositoryId: String,
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
  val linesAdded: Int,
  /**
   * Similar to [linesAdded] but for lines removed.
   * */
  val linesRemoved: Int,

  /**
   * Flag used to tell what the previous filePath was,
   * used to detect renames or moved files.
   * */
  val previousFilePath: String? = null,
)
