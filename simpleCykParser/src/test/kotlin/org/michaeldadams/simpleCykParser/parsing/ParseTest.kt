package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

class ParseTest {
  val yaml = Yaml(configuration = YamlConfiguration(breakScalarsAt = 1_000))

  @Test fun test1(): Unit {
    val x = """
      start: S
      productions:
        S:
          - P: ( S )
          - E: ""
    """.trimIndent()
    val g = x.toYamlMap().toParseRules().toParser()
    val z = arrayOf(Terminal("("), Terminal("("), Terminal(")"), Terminal(")"))
    val chart = Chart(g, *z)
    parse(chart)
    chart.printEntries()
  }

  @Test fun test2(): Unit {
    val x = """
      start: S
      productions:
        S:
          - R: S S S # Recursive
          - E: "" # Empty
          - T: a # Terminal
    """.trimIndent()
    val g = x.toYamlMap().toParseRules().toParser()
    val chart = Chart(g, "a", "a", "a", "a")
    chart.printEntries()
    parse(chart)
  }
}
