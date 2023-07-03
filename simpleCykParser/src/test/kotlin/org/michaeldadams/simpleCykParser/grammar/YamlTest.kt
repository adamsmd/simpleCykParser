package org.michaeldadams.simpleCykParser.grammar.yaml

import kotlin.test.Test
import kotlin.test.assertEquals

import org.michaeldadams.simpleCykParser.grammar.yaml.*

class YamlTest {
  @Test fun test1(): Unit {
    val x = """
      whitespace: "\\s+"
      terminals:
        - A: "A"
        - B: "B"
        - X: "[A-Z]+"
      start: S
      productions:
        S:
          - ( S )
          - X: ""
    """.trimIndent()
    val y = mapFromYamlString(x)
    val g = grammarFromMap(y)
    println(g)
  }
}
