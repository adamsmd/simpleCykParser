package org.michaeldadams.simpleCykParser.parsing

import kotlin.test.Test
import kotlin.test.assertEquals

import org.michaeldadams.simpleCykParser.grammar.yaml.*

class ParserTest {
  @Test fun test1(): Unit {
    val x = """
      start: S
      productions:
        S:
          - ( S )
          - X: ""
    """.trimIndent()
    val y = mapFromYamlString(x)
    /*val l = */parseRulesFromMap(y)
    // println(l)
    // TODO: assertEquals(
    //   listOf(Token("A", 1, 1), Token("B", 3, 3), Token("C", 6, 7)),
    //   Lexer.lex(l, " A B  AA "))
  }
}
