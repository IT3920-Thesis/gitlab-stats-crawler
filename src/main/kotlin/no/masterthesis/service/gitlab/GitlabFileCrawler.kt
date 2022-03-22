package no.masterthesis.service.gitlab

import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import no.masterthesis.util.MailMapParser
import no.masterthesis.util.base64UrlEncode
import reactor.core.publisher.Mono

@Singleton
open class GitlabFileCrawler(
  private val client: GitlabApiClient,
) {

  open fun retrieveMailMap(projectId: String, ref: String = "master"): Map<String, Set<String>> {
    val mailMap = Mono.from(
      client.retrieveFileFromRepository(projectId.base64UrlEncode(), filePath  = ".mailmap", ref = ref)
    ).block()

    return mailMap
      ?.let { MailMapParser.parse(it.contentBase64Decoded) }
      ?: emptyMap()
  }
}