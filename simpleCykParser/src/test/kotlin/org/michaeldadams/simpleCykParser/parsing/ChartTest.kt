package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlPath
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
          - X: ""
    """.trimIndent()
    val g = x.toYamlMap().toParseRules().toProcessed()
    val chart = Chart(g, 5)
    // println(l)
    // TODO: assertEquals(
    //   listOf(Token("A", 1, 1), Token("B", 3, 3), Token("C", 6, 7)),
    //   Lexer.lex(l, " A B  AA "))
    println(chart.symbols.toYaml(YamlPath.root))
    println(Yaml.default.encodeToString(chart.symbols.TheSerializer(), chart.symbols))
  }
}
