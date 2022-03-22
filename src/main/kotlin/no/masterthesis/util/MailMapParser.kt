package no.masterthesis.util

import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

/**
 * Parses .mailmap file contents.
 * Currently, it only extracts the emails (names and comments are removed)
 *
 * @see [https://git-scm.com/docs/gitmailmap](https://git-scm.com/docs/gitmailmap)
 * */
object MailMapParser {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * @return Key is the real email, each value is a commit email that should match to the real email
   * */
  fun parse(rawContents: String): Map<String, Set<String>> {
    val emailMap = rawContents
      .split("\n")
      .map { line ->
        line
          // Remove comments and comments inline (the last replace() removes tha actual comment char)
          .replaceAfter("#", "")
          .replace("#", "")
          .also {
            log.trace("Removed any comments", kv("line", line), kv("strippedLine", it))
          }
          .split(" ")
          .filter(::isEmailMatcher)
          .map(::stripEmailMatcherSyntax)
          // All emails are by definition case-insensitive
          .map { it.lowercase() }
      }
      // Rows that do not contain two items
      .filter { it.size > 1 }
      .groupBy { it.first() }
      // groupBy grouped by the whole value, but we only want the values to
      // consist of the commit email (second value in each entry)
      .mapValues { (_, value) -> value.map { it[1] }.toSet() }

    return emailMap
  }

  private fun isEmailMatcher(word: String) = word.startsWith("<") && word.endsWith(">")
  private fun stripEmailMatcherSyntax(email: String) = email
    .replace("<", "")
    .replace(">", "")
}
