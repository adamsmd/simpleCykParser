package org.michaeldadams.simpleCykParser.yaml

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.MissingRequiredPropertyException
import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.LexRules
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.RhsElement
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.TerminalRule
import org.michaeldadams.simpleCykParser.util.toEqRegex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.text.toRegex

@Suppress("LONG_LINE")
class YamlTest {
  @Test fun testValid(): Unit {
    /*
     * Test various combinations of:
     * - with or without a key
     * - rhs as a YAML list or string
     * - if rhs is a YAML string, then whether there are spaces at beginning or end
     * - rhs components have names or not
     * - symbols in rhs components are terminals or nonterminals
     *
     * TODO: explicit N and T annotations
     *
     * TODO:
     *
     * whitespace: \s+
     * terminals:
     *   - STRING: '"[^"]"'
     *   - NUM: \d+
     *   - IF: if
     *   - (: \(
     *   - STR: (?idmsuxU-idmsuxU) ... TODO (note some flags have no inline)
     * terminalOptions: COMMENTS, UNICODE
     * start: S
     * productions:
     *   S:
     *     - F: if ( S ) then { else }
     *     - F: '" S "'
     *     - S S
     *     - ""
     *     - []
     *     - [X: S, Y: S]
     *     - F: [X: S, S]
     *     - F:
     *        - X: S
     *        - S
     *   T: []
     */

    val actual = """
      whitespace: \s+
      terminals:
        - STRING: "'[^']'"
        - NUM: \d+
        - IF: if
        - (: \(
      start: S
      productions:
        A: []
        B: []
        C: []
        D: []
        E:
          - K: ""
          - ""
        S:
          - K: A t C
          - K: "A u C"
          - K: "A v C "
          - K: " A w C "
          - K: []
          - K: [A, x, C]
          - K: [AA: N:A, BB: x, N:C, y, DD: D]
          - A t C
          - "A u C"
          - "A v C "
          - " A w C "
          - []
          - [A, x, C]
          - [AA: N:A, BB: x, N:C, y, DD: D]
    """.trimIndent().toYamlMap().toGrammar()

    @Suppress(
      "MaxLineLength",
      "ktlint:standard:argument-list-wrapping",
      "ktlint:standard:max-line-length",
    )
    val expected = Grammar(
      LexRules(
        "\\s+".toRegex().toEqRegex(),
        listOf(
          TerminalRule(Terminal("STRING"), "'[^']'".toRegex().toEqRegex()),
          TerminalRule(Terminal("NUM"), "\\d+".toRegex().toEqRegex()),
          TerminalRule(Terminal("IF"), "if".toRegex().toEqRegex()),
          TerminalRule(Terminal("("), "\\(".toRegex().toEqRegex()),
        ),
      ),
      ParseRules(
        Nonterminal("S"),
        mapOf(
          Nonterminal("A") to setOf(),
          Nonterminal("B") to setOf(),
          Nonterminal("C") to setOf(),
          Nonterminal("D") to setOf(),
          Nonterminal("E") to setOf(
            Rhs("K", listOf()),
            Rhs(null, listOf()),
          ),
          Nonterminal("S") to setOf(
            // TODO: shorten
            Rhs("K", listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("t")), RhsElement(null, Nonterminal("C")))),
            Rhs("K", listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("u")), RhsElement(null, Nonterminal("C")))),
            Rhs("K", listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("v")), RhsElement(null, Nonterminal("C")))),
            Rhs("K", listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("w")), RhsElement(null, Nonterminal("C")))),
            Rhs("K", listOf()),
            Rhs("K", listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("x")), RhsElement(null, Nonterminal("C")))),
            Rhs("K", listOf(RhsElement("AA", Nonterminal("A")), RhsElement("BB", Terminal("x")), RhsElement(null, Nonterminal("C")), RhsElement(null, Terminal("y")), RhsElement("DD", Nonterminal("D")))),
            Rhs(null, listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("t")), RhsElement(null, Nonterminal("C")))),
            Rhs(null, listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("u")), RhsElement(null, Nonterminal("C")))),
            Rhs(null, listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("v")), RhsElement(null, Nonterminal("C")))),
            Rhs(null, listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("w")), RhsElement(null, Nonterminal("C")))),
            Rhs(null, listOf()),
            Rhs(null, listOf(RhsElement(null, Nonterminal("A")), RhsElement(null, Terminal("x")), RhsElement(null, Nonterminal("C")))),
            Rhs(null, listOf(RhsElement("AA", Nonterminal("A")), RhsElement("BB", Terminal("x")), RhsElement(null, Nonterminal("C")), RhsElement(null, Terminal("y")), RhsElement("DD", Nonterminal("D")))),
          ),
        ),
      ),
    )

    assertEquals(expected, actual)
  }

  @Test fun testMissing(): Unit {
    // Missing whitespace
    assertFailsWith(MissingRequiredPropertyException::class) {
      """
        # whitespace: \s+
        terminals: []
        start: S
        nonterminals: {}
      """.trimIndent().toYamlMap().toGrammar()
    }
    // Missing terminals
    assertFailsWith(MissingRequiredPropertyException::class) {
      """
        whitespace: \s+
        # terminals: []
        start: S
        nonterminals: {}
      """.trimIndent().toYamlMap().toGrammar()
    }
    // Missing start
    assertFailsWith(MissingRequiredPropertyException::class) {
      """
        whitespace: \s+
        terminals: []
        # start: S
        nonterminals: {}
      """.trimIndent().toYamlMap().toGrammar()
    }
    // Missing nonterminals
    assertFailsWith(MissingRequiredPropertyException::class) {
      """
        whitespace: \s+
        terminals: []
        start: S
        # nonterminals: {}
      """.trimIndent().toYamlMap().toGrammar()
    }
  }

  @Test fun testIncorrectType(): Unit {
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
