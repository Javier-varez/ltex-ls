/* Copyright (C) 2019-2021 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.parsing.markdown

import org.bsplines.ltexls.parsing.CodeAnnotatedTextBuilder
import org.bsplines.ltexls.settings.Settings
import org.junit.platform.suite.api.IncludeEngines
import org.languagetool.markup.AnnotatedText
import kotlin.test.Test
import kotlin.test.assertEquals

@IncludeEngines("junit-jupiter")
class MarkdownAnnotatedTextBuilderTest {
  @Test
  fun testBasicMarkdown() {
    assertPlainText(
      """
      # Heading
      Paragraph with
      multiple lines and [link](example.com)

      """.trimIndent(),
      "Heading\nParagraph with multiple lines and link\n"
    )
    assertPlainText(
      "This is a  \r\ntest.\r\n",
      "This is a test.\n"
    )
    assertPlainText(
      """
      # This is a &copy; Test
      Another [day &ndash; another](example.com) sentence

      """.trimIndent(),
      "This is a \u00a9 Test\nAnother day \u2013 another sentence\n"
    )
    assertPlainText(
      "This is a test: `inline code`.\n\n```\ncode block\n```\n\nThis is another sentence.\n",
      "This is a test: Dummy0.\n\n\n\n\n\nThis is another sentence.\n"
    )
    val markdownNodes: MutableMap<String, String> = HashMap()
    markdownNodes["Code"] = "default"
    markdownNodes["FencedCodeBlock"] = "default"
    assertPlainText(
      "This is a test: `inline code`.\n\n```\ncode block\n```\n\nThis is another sentence.\n",
      "This is a test: inline code.\n\n\ncode block\n\n\nThis is another sentence.\n",
      markdownNodes
    )
  }

  @Test
  fun testDefinitionExtension() {
    assertPlainText(
      """
      Term1
      : Das ist die Definition von *Term1*.

      Term2

      : Das ist die erste Definition von *Term2*.
      : Das ist die zweite Definition von *Term2*.

      : Das ist die dritte Definition von *Term2*.

      """.trimIndent(),
      """
      Term1.
      Das ist die Definition von Term1.

      Term2.

      Das ist die erste Definition von Term2.
      Das ist die zweite Definition von Term2.

      Das ist die dritte Definition von Term2.

      """.trimIndent()
    )
  }

  @Test
  fun testGitLabExtension() {
    assertPlainText(
      "This is a test: \$`E = mc^2`\$.\n\n```math\na^2 + b^2 = c^2\n```\n\nThis is another test.\n",
      "This is a test: Dummy0.\n\n\n\n\n\nThis is another test.\n"
    )
  }

  @Test
  fun testTablesExtension() {
    assertPlainText(
      """
      This is a test.

      | First Column | Second Column |
      | ------------ | ------------- |
      | Interesting  | Super         |

      This is another sentence.

      """.trimIndent(), """
      This is a test.

      First Column Second Column

      Interesting Super

      This is another sentence.

      """.trimIndent()
    )
  }

  @Test
  fun testYamlFrontMatterExtension() {
    assertPlainText(
      """
      ---
      # This is YAML front matter
      - test
      ---

      # Heading
      Test sentence

      """.trimIndent(),
      "\n\n\n\n\nHeading\nTest sentence\n"
    )
    assertPlainText(
      """
      ---
      test: This is a test.
      ---

      # Heading
      Test sentence

      """.trimIndent(),
      "\n\n\n\nHeading\nTest sentence\n"
    )
  }

  @Test
  fun testLtexMarkdownExtension() {
    assertPlainText(
      """
      This is a test: ${"$"}E = mc^2
      $.
      The book is $3, not $5.

      Interesting: $1 \$2 3$.

      ${"$"}$
      a^2 + b^2 = c^2

      ${"$"}$

      This is another test.

      """.trimIndent(), """
      This is a test: Dummy0. The book is $3, not $5.

      Interesting: Dummy1.






      This is another test.

      """.trimIndent()
    )
  }

  companion object {
    private fun assertPlainText(
      code: String,
      expectedPlainText: String,
      markdownNodes: Map<String, String> = emptyMap(),
    ) {
      val annotatedText: AnnotatedText = buildAnnotatedText(code, markdownNodes)
      assertEquals(expectedPlainText, annotatedText.plainText)
    }

    private fun buildAnnotatedText(
      code: String,
      markdownNodes: Map<String, String>
    ): AnnotatedText {
      val builder = CodeAnnotatedTextBuilder.create("markdown")
      val settings = Settings(_markdownNodes = markdownNodes)
      builder.setSettings(settings)
      return builder.addCode(code).build()
    }
  }
}