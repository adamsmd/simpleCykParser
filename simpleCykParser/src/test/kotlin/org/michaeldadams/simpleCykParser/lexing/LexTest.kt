package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toLexRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO
class LexTest {
  @Test fun testLex(): Unit {
    val lexRules = """
      whitespace: "\\s+"
      terminals:
        - A: "A"
        - B: "B B"
        - X: "[A-Z]+"
        - Y: "(?x) \\d+ ( \\.\\d+ )?"
    """.trimIndent().toYamlMap().toLexRules()

    val expectedTokens = listOf(
      Token(Terminal("A"), listOf(MatchGroup("A", IntRange(1, 1)))),
      Token(Terminal("B"), listOf(MatchGroup("B B", IntRange(3, 5)))),
      Token(Terminal("X"), listOf(MatchGroup("AA", IntRange(8, 9)))),
      Token(Terminal("Y"), listOf(MatchGroup("123", IntRange(10, 12)), null)),
      Token(
        Terminal("Y"),
        listOf(MatchGroup("123.45", IntRange(15, 20)), MatchGroup(".45", IntRange(18, 20)))
      )
    )

    assertEquals(Pair(23, expectedTokens), lex(lexRules, " A B B  AA123  123.45  "))
  }
}
