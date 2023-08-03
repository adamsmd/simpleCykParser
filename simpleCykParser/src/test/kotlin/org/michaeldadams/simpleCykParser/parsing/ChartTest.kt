package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.yamlMap
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.RhsElement
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.yaml.toParseRules
import org.michaeldadams.simpleCykParser.yaml.toYaml
import org.michaeldadams.simpleCykParser.yaml.toYamlString
import kotlin.test.Test

// TODO: check consistencey of aux Chart fields

// TODO: more tests
class ChartTest {
  @Test fun test1(): Unit {
    val parseRules = """
      start: S
      productions:
        S:
          - P: ( S )
          - A: S + S
          - E: ""
    """.trimIndent().toYaml().yamlMap.toParseRules()

    val chart = Chart()
    chart.add(3, 4, Terminal("("))
    chart.add(3, 4, Terminal("\""))
    chart.add(2, 4, Nonterminal("S"))
    val elements = listOf(
      RhsElement(null, Nonterminal("S")),
      RhsElement(null, Terminal("+")),
      RhsElement(null, Nonterminal("S")),
    )
    val item = Item(Nonterminal("S"), Rhs("A", elements), 2)

    chart.add(2, 4, item, null)
    chart.add(1, 4, item.consume()!!.second, null)
    chart.addUnconsumedItemEntries(parseRules)
    println(chart.toYamlString())
  }

  @Test fun test2(): Unit {
    val parseRules = """
      start: S
      productions:
        S:
          - P: ( S )
          - E: ""
    """.trimIndent().toYaml().yamlMap.toParseRules()

    val chart = Chart()
    chart.add(listOf(Terminal("("), Terminal("("), Terminal(")"), Terminal(")")))
    chart.addUnconsumedItemEntries(parseRules)
    println(chart.toYamlString())
  }

  @Test fun test3(): Unit {
    val parseRules = """
      start: S
      productions:
        S:
          - R: S S S # Recursive
          - E: "" # Empty
          - T: a # Terminal
    """.trimIndent().toYaml().yamlMap.toParseRules()

    val chart = Chart()
    chart.add(listOf("a", "a", "a", "a").map { Terminal(it) })
    chart.addUnconsumedItemEntries(parseRules)
    println(chart.toYamlString())
  }
}
