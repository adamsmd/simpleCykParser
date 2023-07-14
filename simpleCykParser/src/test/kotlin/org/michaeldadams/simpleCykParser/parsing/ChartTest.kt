package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

// TODO
class ChartTest {
  @Test fun test1(): Unit {
    val parser = """
      start: S
      productions:
        S:
          - P: ( S )
          - A: S + S
          - E: ""
    """.trimIndent().toYamlMap().toParseRules().toParser()

    val chart = Chart(parser)
    chart.add(3, 4, Terminal("("))
    chart.add(2, 4, Nonterminal("S"))
    val lhs = Nonterminal("S")
    val parts = listOf(null to Nonterminal("S"), null to Terminal("+"), null to Nonterminal("S"))
    val rhs = Rhs("A", parts)

    chart.add(2, 4, lhs, rhs, 2, null)
    chart.add(1, 4, lhs, rhs, 3, null)
    chart.printEntries()
  }
}
