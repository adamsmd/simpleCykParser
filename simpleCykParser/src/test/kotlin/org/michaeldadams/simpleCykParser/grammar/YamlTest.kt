package org.michaeldadams.simpleCykParser.grammar

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.MissingRequiredPropertyException
import org.michaeldadams.simpleCykParser.util.toEqRegex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.text.toRegex

class YamlTest {
  // @Test fun test1(): Unit {
  //   val x = """
  //     whitespace: "\\s+"
  //     terminals:
  //       - A: "A"
  //       - B: "B"
  //       - X: "[A-Z]+"
  //     start: S
  //     productions:
  //       S:
  //         - ( S )
  //         - X: ""
  //   """.trimIndent()
  //   val y = x.toYamlMap()
  //   /*var g = */y.toGrammar()
  //   // println(g)
  //   // TODO: assertEqual(grammar, g)
  // }

  // @Test fun test2(): Unit {
  //   val x = """
  //     whitespace: \s+
  //     terminals:
  //       - STRING: '"[^"]"'
  //       - NUM: \d+
  //       - IF: if
  //       - (: \(
  //     start: S
  //     productions:
  //       S:
  //         - F: if ( S ) then { else }
  //         - F: '" S "'
  //         - S S
  //         - ""
  //       T: []
  //   """.trimIndent()
  //   val y = x.toYamlMap()
  //   /*var g = */y.toGrammar()
  //   // println(g)
  //   // TODO: assertEqual(grammar, g)
  // }

  @Test fun testValid(): Unit {
    // TODO: explain
    // key or not
    // rhs is list or string
    // item is scalar or pair
    // symbol is token or not
    // string with spaces at begining or end
    val actual = """
      whitespace: \s+
      terminals:
        - STRING: '"[^"]"'
        - NUM: \d+
        - IF: if
        - (: \(
      start: S
      productions:
        A: []
        B: []
        C: []
        D: []
        S:
          - K: A t C
          - K: "A u C"
          - K: "A v C "
          - K: " A w C "
          - K: []
          - K: [A, x, C]
          - K: [AA: A, BB: x, C, y, DD: D]
          - A t C
          - "A u C"
          - "A v C "
          - " A w C "
          - []
          - [A, x, C]
          - [AA: A, BB: x, C, y, DD: D]
    """.trimIndent().toYamlMap().toGrammar()

    // TODO: list vs sequence
    @Suppress("MaxLineLength")
    val expected = Grammar(
      LexRules(
        "\\s+".toRegex().toEqRegex(),
        listOf(
          TerminalRule(Terminal("STRING"), "\"[^\"]\"".toRegex().toEqRegex()),
          TerminalRule(Terminal("NUM"), "\\d+".toRegex().toEqRegex()),
          TerminalRule(Terminal("IF"), "if".toRegex().toEqRegex()),
          TerminalRule(Terminal("("), "\\(".toRegex().toEqRegex())
        )
      ),
      ParseRules(
        Nonterminal("S"),
        mapOf(
          Nonterminal("A") to setOf(),
          Nonterminal("B") to setOf(),
          Nonterminal("C") to setOf(),
          Nonterminal("D") to setOf(),
          Nonterminal("S") to setOf(
            Production(Nonterminal("S"), "K", listOf(null to Nonterminal("A"), null to Terminal("t"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), "K", listOf(null to Nonterminal("A"), null to Terminal("u"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), "K", listOf(null to Nonterminal("A"), null to Terminal("v"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), "K", listOf(null to Nonterminal("A"), null to Terminal("w"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), "K", listOf()),
            Production(Nonterminal("S"), "K", listOf(null to Nonterminal("A"), null to Terminal("x"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), "K", listOf("AA" to Nonterminal("A"), "BB" to Terminal("x"), null to Nonterminal("C"), null to Terminal("y"), "DD" to Nonterminal("D"))),
            Production(Nonterminal("S"), null, listOf(null to Nonterminal("A"), null to Terminal("t"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), null, listOf(null to Nonterminal("A"), null to Terminal("u"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), null, listOf(null to Nonterminal("A"), null to Terminal("v"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), null, listOf(null to Nonterminal("A"), null to Terminal("w"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), null, listOf()),
            Production(Nonterminal("S"), null, listOf(null to Nonterminal("A"), null to Terminal("x"), null to Nonterminal("C"))),
            Production(Nonterminal("S"), null, listOf("AA" to Nonterminal("A"), "BB" to Terminal("x"), null to Nonterminal("C"), null to Terminal("y"), "DD" to Nonterminal("D")))
          )
        )
      )
    )

    assertEquals(expected, actual)
  }

  @Test fun testInvalid(): Unit {
    // TODO: missing entries
    assertFailsWith(MissingRequiredPropertyException::class) {
      """
        terminals: []
        start: S
      """.trimIndent().toYamlMap().toGrammar()
    }
    // Map that should represent a pair but has multiple entries
    assertFailsWith(IllegalArgumentException::class) {
      """
        whitespace: \s+
        terminals:
          - A: A
            B: B
        start: S
        productions: []
      """.trimIndent().toYamlMap().toGrammar()
    }
    // Map that should represent a pair but has no entries
    assertFailsWith(IllegalArgumentException::class) {
      """
        whitespace: \s+
        terminals:
          - {}
        start: S
        productions: []
      """.trimIndent().toYamlMap().toGrammar()
    }
    assertFailsWith(IncorrectTypeException::class) {
      """
        whitespace: \s+
        terminals: []
        start: S
        productions:
          S:
            - # Null entry
      """.trimIndent().toYamlMap().toGrammar()
    }
    assertFailsWith(IncorrectTypeException::class) {
      """
        whitespace: \s+
        terminals: []
        start: S
        productions:
          S:
            - F: {A: x}
      """.trimIndent().toYamlMap().toGrammar()
    }
    assertFailsWith(IncorrectTypeException::class) {
      """
        whitespace: \s+
        terminals: []
        start: S
        productions:
          S:
            - [A, [B]]
      """.trimIndent().toYamlMap().toGrammar()
    }
  }
}
