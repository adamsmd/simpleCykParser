package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

class ChartTest {
  @Test fun test1(): Unit {
    val x = """
      start: S
      productions:
        S:
          - ( S )
          - S + S
          - X: ""
    """.trimIndent()
    val g = x.toYamlMap().toParseRules().toProcessed()
    val chart = Chart(g, 5)
    chart.addSymbol(3, 4, Terminal("("), null)
    chart.addSymbol(2, 4, Terminal("("), Production(Nonterminal("S"), "X", emptyList()))
    val p = PartialProduction(
      Production(Nonterminal("S"), null, listOf(null to Nonterminal("S"), null to Terminal("+"), null to Nonterminal("S"))),
      2
    )

    chart.addProduction(2, 4, p, null)
    chart.addProduction(1, 4, p.toNext()!!.first, null)
    println(Yaml.default.encodeToString(SymbolsSerializer(), chart))
    println(Yaml.default.encodeToString(ProductionsSerializer(), chart))
  }
}
