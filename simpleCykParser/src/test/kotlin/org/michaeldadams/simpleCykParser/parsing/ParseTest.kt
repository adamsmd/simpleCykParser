package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

// TODO: More tests
class ParseTest {
  val yaml = Yaml(configuration = YamlConfiguration(breakScalarsAt = 1000))

  @Test fun test1(): Unit {
    val parser = """
      start: S
      productions:
        S:
          - P: ( S )
          - E: ""
    """.trimIndent().toYamlMap().toParseRules().toParser()

    val chart = Chart(parser)
    chart.add(listOf(Terminal("("), Terminal("("), Terminal(")"), Terminal(")")))
    parse(chart)
    chart.printEntries()
  }

  @Test fun test2(): Unit {
    val parser = """
      start: S
      productions:
        S:
          - R: S S S # Recursive
          - E: "" # Empty
          - T: a # Terminal
    """.trimIndent().toYamlMap().toParseRules().toParser()

    val chart = Chart(parser)
    chart.add(listOf("a", "a", "a", "a").map { Terminal(it) })
    parse(chart)
    chart.printEntries()
  }
}
