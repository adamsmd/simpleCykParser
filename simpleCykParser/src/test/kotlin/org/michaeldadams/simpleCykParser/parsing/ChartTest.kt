package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.RhsElement
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
    chart.add(3, 4, Terminal("\""))
    chart.add(2, 4, Nonterminal("S"))
    val elements = listOf(
      RhsElement(null, Nonterminal("S")),
      RhsElement(null, Terminal("+")),
      RhsElement(null, Nonterminal("S")))
    val item = Item(Nonterminal("S"), Rhs("A", elements), 2)

    chart.add(2, 4, item, null)
    chart.add(1, 4, item.consume()!!.second, null)
    chart.printEntries()
  }
}
