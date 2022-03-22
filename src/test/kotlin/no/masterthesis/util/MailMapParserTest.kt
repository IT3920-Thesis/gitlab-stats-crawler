package no.masterthesis.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.hasEntry

internal class MailMapParserTest {
  @ParameterizedTest
  @ValueSource(strings = [
    "dummy1@TeTs.com",
    "DUmmY@test.COm",
  ])
  fun `'parse' Matches case insensitive emails`(commitEmail: String) {
    val mailMap = """
      <realmail@example.org> <$commitEmail>
    """.trimIndent()

    val parsed = MailMapParser.parse(mailMap)

    expectThat(parsed).hasEntry("realmail@example.org", setOf(commitEmail.lowercase()))
  }

  @Test
  fun `'parse' Ignore Real name in mailmap`() {
    val mailMap = """
      Real name <realmail@example.org> <dummy1@email.com>
    """.trimIndent()

    val parsed = MailMapParser.parse(mailMap)

    expectThat(parsed).hasEntry("realmail@example.org", setOf("dummy1@email.com"))
  }

  @Test
  fun `'parse' Ignore Matcher name in mailmap`() {
    val mailMap = """
      Real name <realmail@example.org> Dummy name <dummy1@email.com>
    """.trimIndent()

    val parsed = MailMapParser.parse(mailMap)

    expectThat(parsed).hasEntry("realmail@example.org", setOf("dummy1@email.com"))
  }

  @Test
  fun `'parse' allow multiple emails to to match the same real email`() {
    val mailMap = """
      <realmail@example.org> <dummy1@email.com>
      <realmail@example.org> <dummy2@email.com>
      <realmail@example.org> <dummy3@email.com>
    """.trimIndent()

    val parsed = MailMapParser.parse(mailMap)

    expectThat(parsed).hasEntry(
      "realmail@example.org",
      setOf(
        "dummy1@email.com",
        "dummy2@email.com",
        "dummy3@email.com",
      )
    )
  }

  @Test
  fun `'parse' ignores comments at separate rows`() {
    val mailMap = """
      # This is a comment
      <realmail@example.org> <dummy1@email.com>
    """.trimIndent()

    val parsed = MailMapParser.parse(mailMap)

    expectThat(parsed).hasEntry("realmail@example.org", setOf("dummy1@email.com"))
  }

  @Test
  fun `'parse' ignores comments inline`() {
    val mailMap = """
      <realmail@example.org> <dummy1@email.com> # This is a comment
    """.trimIndent()

    val parsed = MailMapParser.parse(mailMap)

    expectThat(parsed).hasEntry("realmail@example.org", setOf("dummy1@email.com"))
  }

  @Test
  fun `'parse' ignores comments inline missing spacing`() {
    val mailMap = """
      <realmail@example.org> <dummy1@email.com># This is a comment
    """.trimIndent()

    val parsed = MailMapParser.parse(mailMap)

    expectThat(parsed).hasEntry("realmail@example.org", setOf("dummy1@email.com"))
  }
}
