package org.michaeldadams.simpleCykParser.parsing

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
    /*val l = */y.toParseRules()
    // println(l)
    // TODO: assertEquals(
    //   listOf(Token("A", 1, 1), Token("B", 3, 3), Token("C", 6, 7)),
    //   Lexer.lex(l, " A B  AA "))
  }
}
