package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

class ParserTest {
  @Test fun test1(): Unit {
    val x = """
      start: S
      productions:
        S:
          - ( S )
          - X: ""
    """.trimIndent()
    val y = x.toYamlMap()
    val g = x.toYamlMap().toParseRules().toProcessed()
    val z = arrayOf(Terminal("("), Terminal("("), Terminal(")"), Terminal(")"))
    val chart = Chart(g, *z)
    parse(chart)
    println(Yaml.default.encodeToString(SymbolsSerializer(), chart))
    println(Yaml.default.encodeToString(ProductionsSerializer(), chart))
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
    val y = x.toYamlMap()
    val g = x.toYamlMap().toParseRules().toProcessed()
    val chart = Chart(g, "a", "a", "a", "a")
    parse(chart)
    val yaml = Yaml(configuration = YamlConfiguration(breakScalarsAt = 1000))
    println(yaml.encodeToString(SymbolsSerializer(), chart))
    println(yaml.encodeToString(ProductionsSerializer(), chart))
  }
}
