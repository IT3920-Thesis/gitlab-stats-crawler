package no.masterthesis.handler.changecontribution

import java.time.ZonedDateTime
import no.masterthesis.domain.changecontribution.ContributionType
import no.masterthesis.handler.changecontribution.ChangeContributionClassifier.predictContributionType
import no.masterthesis.service.gitlab.GitCommit
import no.masterthesis.service.gitlab.GitlabGitCommitDiff
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEqualTo

internal class ChangeContributionClassifierTest {
  @Test
  fun `'predictContributionTypes' predicts Java functional code`() {
    val commit = generateCommit(listOf(
      generateSimpleDiff("src/main/java/MyApp.java"),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.FUNCTIONAL)
    }
  }

  @Test
  fun `'predictContributionTypes' predicts Java test code`() {
    val commit = generateCommit(listOf(
      generateSimpleDiff("src/test/java/MyAppTest.java"),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.TEST)
    }
  }

  @Test
  fun `'predictContributionTypes' predicts JavaScript and TypeScript functional code`() {
    val commit = generateCommit(listOf(
      generateSimpleDiff("client/src/components/InformationSection/index.js"),
      generateSimpleDiff("routes/graphql/searchMedia.js"),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.FUNCTIONAL)
    }
  }

  @Test
  fun `'predictContributionTypes' predicts Stylesheets as functional code`() {
    val commit = generateCommit(listOf(
      generateSimpleDiff("client/src/components/ToggleButtonGroup.less"),
      generateSimpleDiff("client/src/components/ToggleButtonGroup.css"),
      generateSimpleDiff("client/src/components/ToggleButtonGroup.sass"),
      generateSimpleDiff("client/src/components/ToggleButtonGroup.scss"),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.FUNCTIONAL)
    }
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "client/public/index.html",
    "server/template/main.twig",
  ])
  fun `'predictContributionTypes' predicts HTML files as functional code`(fileName: String) {
    val commit = generateCommit(listOf(
      generateSimpleDiff(fileName),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.FUNCTIONAL)
    }
  }

  @ParameterizedTest
  @ValueSource(strings = [
    ".eslintrc",
    ".eslintrc.js",
    ".eslintrc.json",
    ".eslintignore",
    ".prettierrc",
    ".prettierrc.js",
    "prettier.config.js",
    "src/main/resources/something_checkstyle.xml",
  ])
  fun `'predictContributionTypes' predicts linter configs as Configuration`(fileName: String) {
    val commit = generateCommit(listOf(
      generateSimpleDiff(fileName),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.CONFIGURATION)
    }
  }

  @ParameterizedTest
  @ValueSource(strings = [
    ".gitignore",
    ".editorconfig",
    ".dockerignore",
    "compose.yml",
    "docker-compose.yml",
    "Dockerfile",
    "Dockerfile.dev",
    "build.gradle",
    "settings.gradle",
    "build.gradle.kts",
    "pom.xml",
    "secrets.env",
    "nginx.conf",
    "src/main/resources/application.properties",
    "random_xml_file.xml",
    "package.json",
    "package-lock.json",
    "tsconfig.json",
    ".babelrc",
    ".babelrc.json",
    ".babelrc.js",
    "babel.config.js",
    "babel.config.json",
  ])
  fun `'predictContributionTypes' predicts typical config files as Configuration`(fileName: String) {
    val commit = generateCommit(listOf(
      generateSimpleDiff(fileName),
    ))

    val contributions = commit.diffs.map { predictContributionType(it) }

    expectThat(contributions).all {
      isEqualTo(ContributionType.CONFIGURATION)
    }
  }

  private fun generateSimpleDiff(fileName: String) = GitlabGitCommitDiff(
    oldPath = fileName,
    newPath = fileName,
    aMode = "100644",
    bMode = "100644",
    isNewFile = false,
    isFileRenamed = false,
    isFileDeleted = false,
    diff = "",
  )

  private fun generateCommit(diffs: List<GitlabGitCommitDiff>) = GitCommit(
    id = "123123",
    committer = GitCommit.Author(name = null, "test@example.org"),
    message = null,
    createdAt = ZonedDateTime.now(),
    title = "Add functional tests",
    diffs = diffs,
  )
}
