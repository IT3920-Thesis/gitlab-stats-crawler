package no.masterthesis.event

import no.masterthesis.service.gitlab.GitCommit

/**
 * Event payload for custom published events,
 * which can be listened to with [io.micronaut.runtime.event.annotation.EventListener]
 * */
data class GitlabCommitEvent(
  val groupId: String,
  val commit: GitCommit,
)
