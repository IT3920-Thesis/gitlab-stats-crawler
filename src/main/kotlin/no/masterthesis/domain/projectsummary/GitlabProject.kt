package no.masterthesis.domain.projectsummary

import java.time.ZonedDateTime

data class CodeQualityTool(
  val name: String,
  val type: CodeQualityToolType
) {
  enum class CodeQualityToolType {
    TEST,
    LINTER,
  }
}

/**
 *
 * @property groupId This is the name of the project's subgroup (We know the rootGroup)
 * @property projectId
 * @property timeSeen Time we checked the project.
 * @property codeQualityTools List of code quality tools included in this project
 * @property illegalFolders These are folders / file paths that shouldn't be present in the project
 * */
data class GitlabProject (
  val groupId: String,
  val projectId: String,
  val timeSeen: ZonedDateTime = ZonedDateTime.now(),
  val codeQualityTools: List<CodeQualityTool> = emptyList(),
  val illegalFolders: Set<String> = emptySet(),
)
