package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.toParseRules
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import org.michaeldadams.simpleCykParser.parsing.parse
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
}
