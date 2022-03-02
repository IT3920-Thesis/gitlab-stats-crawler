package no.masterthesis.handler.changecontribution

import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.changecontribution.ChangeContribution
import no.masterthesis.domain.changecontribution.ChangeContributionRepository
import no.masterthesis.event.GitlabCommitEvent
import no.masterthesis.handler.changecontribution.ChangeContributionClassifier.predictContributionType
import no.masterthesis.handler.changecontribution.GitDiffParser.countLinesChanged
import no.masterthesis.service.gitlab.GitCommit
import org.slf4j.LoggerFactory

@Singleton
internal open class ChangeContributionListener(
  @Inject private val repository: ChangeContributionRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @EventListener
  @Async
  open fun onCommit(event: GitlabCommitEvent) {
    log.info("Received new commit event",
      kv("groupId", event.groupId),
      kv("repositoryId", event.repositoryId),
      kv("commitSha", event.commit.id),
    )
    val commit = event.commit

    val contributions = commit.diffs.flatMap {
      extractContributions(
        groupId = event.groupId,
        repositoryId = event.repositoryId,
        commit = commit,
      )
    }
    log.trace("Extracted contributions", kv("contributions", contributions), kv("groupId", event.groupId), kv("repositoryId", event.repositoryId))
    repository.saveAll(contributions)
  }

  private fun extractContributions(groupId: String, repositoryId: String, commit: GitCommit): List<ChangeContribution> {
    return commit.diffs.flatMap {
      val contributionType = predictContributionType(it)
      val (linesAdded, linesRemoved) = countLinesChanged(it)

      // Contribution made by the main author
      val primaryContribution = ChangeContribution(
        commitSha = commit.id,
        groupId = groupId,
        repositoryId = repositoryId,
        authorEmail = commit.committer.email,
        filePath = it.newPath,
        createdOn = commit.createdAt,
        type = contributionType,
        linesAdded = linesAdded,
        linesRemoved = linesRemoved,
      )

      val coContributions = extractCoAuthors(commit).map { coAuthor ->
        // Everything in the contribution is the same, exception the co-authors email
        primaryContribution.copy(authorEmail = coAuthor.email)
      }

      coContributions.plus(primaryContribution)
    }
  }

  /**
   * Searches the commit message for lines starting with "Co-Authored-By",
   * which indicates that these contributions are made by multiple author
   * */
  private fun extractCoAuthors(commit: GitCommit): List<GitCommit.Author> {
    return commit.message
      ?.split("\n")
      ?.filter { it.startsWith("co-authored-by: ", ignoreCase = true) }
      ?.map {
        val coAuthor = it
          .replace("co-authored-by: ", "", ignoreCase = true)
          .split(" ")

        val email = coAuthor.last().replace("<", "").replace(">", "")
        GitCommit.Author(
          name = coAuthor.first(),
          email = email,
        )
      }
      ?: emptyList()
  }
}
