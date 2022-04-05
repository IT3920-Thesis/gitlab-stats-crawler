package no.masterthesis.util

import org.slf4j.LoggerFactory

object MailMapUtil {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun overrideCommitEmails(mailMap: Map<String, Set<String>>, commitEmail: String): String {
    val emailLookup = mailMap
      // Invert the mailmap so we can match on commit email
      .flatMap { (key, values) -> values.map { it to key } }
      .associate { it }

    val realEmail = emailLookup[commitEmail.lowercase()];
    return realEmail ?: commitEmail
  }
}
