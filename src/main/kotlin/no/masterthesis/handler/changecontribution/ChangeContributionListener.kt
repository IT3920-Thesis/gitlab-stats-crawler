package no.masterthesis.handler.changecontribution

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.masterthesis.domain.changecontribution.ChangeContribution
import no.masterthesis.domain.changecontribution.ChangeContributionRepository
import no.masterthesis.factory.RABBITMQ_CRAWL_CONTRIBUTION_ID
import no.masterthesis.handler.GitlabCrawlProjectEvent
import no.masterthesis.util.ChangeContributionClassifier.predictContributionType
import no.masterthesis.handler.changecontribution.GitDiffParser.countLinesChanged
import no.masterthesis.handler.commitaggregate.CommitQualityClassifier.isMergeCommit
import no.masterthesis.service.gitlab.GitCommit
import no.masterthesis.service.gitlab.GitlabCommitCrawler
import no.masterthesis.service.gitlab.GitlabFileCrawler
import org.slf4j.LoggerFactory

@Singleton
@RabbitListener
internal open class ChangeContributionListener(
  @Inject private val repository: ChangeContributionRepository,
  @Inject private val fileCrawler: GitlabFileCrawler,
  @Inject private val commitCrawler: GitlabCommitCrawler,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Crawls and classifies the "Change contributions" (See domain [ChangeContribution]),
   * on a per-project basis.
   *
   * Triggers when a project is pushed to [RABBITMQ_CRAWL_CONTRIBUTION_ID].
   * In case of transient failures, such as rate-limiting or connection problems with GitLab,
   * [onProject] will be retried with the same project if it throws an exception.
   * */
  @Queue(RABBITMQ_CRAWL_CONTRIBUTION_ID, numberOfConsumers = 1)
  fun onProject(event: GitlabCrawlProjectEvent) {
    log.info(
      "Received new project to crawl change contribution from",
      kv("event", event),
    )

    val commits = commitCrawler.findAllCommitsByProject(event.projectId)
    log.info("Commits retrieved", kv("projectId", event.projectId), kv("commits", commits.size))

    commits
      // Merge commits should be excluded because they often double count contributions
      .filter { !isMergeCommit(it) }
      .map { classifyChangeContribution(event, it) }
      // Save contributions in batches in case a single commit has a lot of changes
      .forEach {
        repository.saveAll(it)
        log.trace("Contributions saved to database", kv("contributions", it))
      }

    log.info("Completed change contribution crawling", kv("event", event))
  }

  private fun classifyChangeContribution(event: GitlabCrawlProjectEvent, commit: GitCommit): List<ChangeContribution> {
    val contributions = commit.diffs
      .flatMap {
        extractContributions(
          groupId = event.subGroupId,
          repositoryId = event.projectPath,
          commit = commit,
        )
      }
      .map {
        overrideCommitEmails(it, projectId = event.projectId.toString(), branch = event.defaultBranch)
      }

    log.trace(
      "Extracted contributions",
      kv("contributions", contributions),
      kv("subGroupId", event.subGroupId),
      kv("projectPath", event.projectPath),
    )
    return contributions
  }

  private fun overrideCommitEmails(contribution: ChangeContribution, projectId: String, branch: String): ChangeContribution {
    val mailMap = fileCrawler.retrieveMailMap(projectId, branch)
    val emailLookup = mailMap
      // Invert the mailmap so we can match on commit email
      .flatMap { (key, values) -> values.map { it to key } }
      .associate { it }

    val realEmail = emailLookup[contribution.authorEmail.lowercase()];
    if (realEmail != null) {
      log.trace("Overriding commit email with real email", kv("commitEmail", contribution.authorEmail), kv("realEmail", realEmail))
      return contribution.copy(authorEmail = realEmail)
    }

    return contribution
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

        isFileNew = it.isNewFile,
        isFileDeleted = it.isFileDeleted,
        previousFilePath = if (it.oldPath !== it.newPath) it.oldPath else null,
      )

      // Multiple people may have contributed to the same file
      // we expect this to be flagged using "co-authored-by:" in commit messages
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
