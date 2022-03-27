package no.masterthesis.service.gitlab

import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import no.masterthesis.util.MailMapParser
import no.masterthesis.util.encodeUriComponent
import reactor.core.publisher.Mono

@Singleton
open class GitlabFileCrawler(
  private val client: GitlabApiClient,
) {

  @Cacheable("mailmap", parameters = ["projectId"])
  open fun retrieveMailMap(projectId: String, ref: String = "master"): Map<String, Set<String>> {
    val mailMap = Mono.from(
      client.retrieveFileFromRepository(projectId.encodeUriComponent(), filePath  = ".mailmap", ref = ref)
    ).block()

    return mailMap
      ?.let { MailMapParser.parse(it.contentBase64Decoded) }
      ?: emptyMap()
  }
}
