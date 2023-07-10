package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

// TODO
class ChartTest {
  @Test fun test1(): Unit {
    val x = """
      start: S
      productions:
        S:
          - P: ( S )
          - A: S + S
          - E: ""
    """.trimIndent()
    val g = x.toYamlMap().toParseRules().toParser()
    val chart = Chart(g, 5)
    chart.addSymbol(3, 4, Terminal("("))
    chart.addSymbol(2, 4, Nonterminal("S")) // TODO: should this be "S"?
    val rhs = listOf(null to Nonterminal("S"), null to Terminal("+"), null to Nonterminal("S"))
    val prod = Production(Nonterminal("S"), "A", rhs)

    chart.addProduction(2, 4, prod, 2, null)
    chart.addProduction(1, 4, prod, 3, null)
    chart.printEntries()
  }
}
