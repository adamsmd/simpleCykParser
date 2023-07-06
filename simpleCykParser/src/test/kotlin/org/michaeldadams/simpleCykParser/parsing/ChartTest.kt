package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
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
    chart.addSymbol(Pair(3, 4) to Pair(Terminal("("), null))
    chart.addSymbol(Pair(2, 4) to Pair(Terminal("("), Production(Nonterminal("S"), "X", emptyList())))
    val p = PartialProduction(
      Production(Nonterminal("S"), null, listOf(Nonterminal("S"), Terminal("+"), Nonterminal("S"))),
      2
    )

    chart.addProduction(Pair(2, 4) to Pair(p, null))
    chart.addProduction(Pair(1, 4) to Pair(p.consume()!!.first, null))
    println(Yaml.default.encodeToString(SymbolsSerializer(), chart))
    println(Yaml.default.encodeToString(ProductionsSerializer(), chart))
  }
}
