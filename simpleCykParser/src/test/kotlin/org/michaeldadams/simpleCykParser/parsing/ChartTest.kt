package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.RhsElement
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

// TODO: mote tests
class ChartTest {
  @Test fun test1(): Unit {
    val parseRules = """
      start: S
      productions:
        S:
          - P: ( S )
          - A: S + S
          - E: ""
    """.trimIndent().toYamlMap().toParseRules()

    val chart = Chart(parseRules)
    chart.add(3, 4, Terminal("("))
    chart.add(3, 4, Terminal("\""))
    chart.add(2, 4, Nonterminal("S"))
    val elements = listOf(
      RhsElement(null, Nonterminal("S")),
      RhsElement(null, Terminal("+")),
      RhsElement(null, Nonterminal("S")))
    val item = Item(Nonterminal("S"), Rhs("A", elements), 2)

    chart.add(2, 4, item, null)
    chart.add(1, 4, item.consume()!!.second, null)
    chart.addEpsilonItems()
    chart.printEntries()
  }

  @Test fun test2(): Unit {
    val parseRules = """
      start: S
      productions:
        S:
          - P: ( S )
          - E: ""
    """.trimIndent().toYamlMap().toParseRules()

    val chart = Chart(parseRules)
    chart.add(listOf(Terminal("("), Terminal("("), Terminal(")"), Terminal(")")))
    // parse(chart)
    chart.addEpsilonItems()
    chart.printEntries()
  }

  @Test fun test3(): Unit {
    val parseRules = """
      start: S
      productions:
        S:
          - R: S S S # Recursive
          - E: "" # Empty
          - T: a # Terminal
    """.trimIndent().toYamlMap().toParseRules()

    val chart = Chart(parseRules)
    chart.add(listOf("a", "a", "a", "a").map { Terminal(it) })
    // parse(chart)
    chart.addEpsilonItems()
    chart.printEntries()
  }
}
