package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.toGrammar
import org.michaeldadams.simpleCykParser.grammar.toYamlMap
import kotlin.test.Test

class ChartTest {
  @Test fun test1(): Unit {
    val x = """
      whitespace: ""
      terminals:
        - A: "A"
      start: S
      productions:
        S:
          - ( S )
          - X: ""
    """.trimIndent()
    val g = x.toYamlMap().toGrammar().toProcessed()
    val chart = Chart(g, 5)
    // println(l)
    // TODO: assertEquals(
    //   listOf(Token("A", 1, 1), Token("B", 3, 3), Token("C", 6, 7)),
    //   Lexer.lex(l, " A B  AA "))
  }
}
