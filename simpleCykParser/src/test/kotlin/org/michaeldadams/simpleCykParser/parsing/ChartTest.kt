package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlPath
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
    chart.symbols += Pair(3, 4) to Pair(Terminal("("), null)
    chart.symbols += Pair(2, 4) to Pair(Terminal("("), Production(Nonterminal("S"), "X", emptyList()))
    val p = PartialProduction(
      Production(Nonterminal("S"), null, listOf(Nonterminal("S"), Terminal("+"), Nonterminal("S"))),
      2
    )

    chart.productions += Pair(2, 4) to Pair(p, null)
    chart.productions += Pair(1, 4) to Pair(p.consume()!!, null)
    println(Yaml.default.encodeToString(chart.symbols.Serializer(), chart.symbols))
    println(Yaml.default.encodeToString(chart.productions.Serializer(), chart.productions))
  }
}
