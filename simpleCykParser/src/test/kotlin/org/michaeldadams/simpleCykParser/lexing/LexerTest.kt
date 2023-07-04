package org.michaeldadams.simpleCykParser.lexing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

import org.michaeldadams.simpleCykParser.grammar.yaml.mapFromYamlString
import org.michaeldadams.simpleCykParser.grammar.yaml.lexRulesFromMap
import org.michaeldadams.simpleCykParser.grammar.Terminal

class LexerTest {
  @Test fun testTokenConstruction(): Unit {
    assertFailsWith(IllegalArgumentException::class) { Token(Terminal("A"), listOf()) }
    assertFailsWith(IllegalArgumentException::class) { Token(Terminal("A"), listOf(null)) }
    Token(Terminal("A"), listOf(MatchGroup("A", IntRange(1, 1))))
  }

  @Test fun testLexing(): Unit {
    val x = """
      whitespace: "\\s+"
      terminals:
        - A: "A"
        - B: "B B"
        - X: "[A-Z]+"
        - Y: "(?x) \\d+ ( \\.\\d+ )?"
    """.trimIndent()

    val y = mapFromYamlString(x)
    val l = lexRulesFromMap(y)

    val expectedTokens = listOf(
      Token(Terminal("A"), listOf(MatchGroup("A", IntRange(1, 1)))),
      Token(Terminal("B"), listOf(MatchGroup("B B", IntRange(3, 5)))),
      Token(Terminal("X"), listOf(MatchGroup("AA", IntRange(8, 9)))),
      Token(Terminal("Y"), listOf(MatchGroup("123", IntRange(11, 13)), null)),
      Token(
        Terminal("Y"),
        listOf(MatchGroup("123.45", IntRange(15, 20)), MatchGroup(".45", IntRange(18, 20)))))

    assertEquals(
      Pair(23, expectedTokens),
      lex(l, " A B B  AA 123 123.45  "))
  }
}
