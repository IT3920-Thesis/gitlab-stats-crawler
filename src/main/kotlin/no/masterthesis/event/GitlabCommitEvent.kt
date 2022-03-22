package no.masterthesis.event

import no.masterthesis.service.gitlab.GitCommit

/**
 * Event payload for custom published events,
 * which can be listened to with [io.micronaut.runtime.event.annotation.EventListener]
 * */
data class GitlabCommitEvent(
  val rootGroupId: String,
  val groupId: String,
  val repositoryPath: String,
  val projectId: Long,
  val defaultBranch: String = "master",
  val commit: GitCommit,
)
