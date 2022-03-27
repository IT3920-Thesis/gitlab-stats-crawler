package no.masterthesis.domain.gitlabproject

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
 * @property groupId
 * @property projectId
 * @property codeQualityTools List of code quality tools included in this project
 * @property illegalFolders These are folders / file paths that shouldn't be present in the project
 * */
data class GitlabProject (
  val groupId: String,
  val projectId: String,
  val codeQualityTools: List<CodeQualityTool> = emptyList(),
  val illegalFolders: Set<String> = emptySet(),
)
