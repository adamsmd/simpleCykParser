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
    /*var g = */grammarFromMap(y)
    // println(g)
    // TODO: assertEqual(grammar, g)
  }

  @Test fun test2(): Unit {
    val x = """
      whitespace: \s+
      terminals:
        - STRING: '"[^"]"'
        - NUM: \d+
        - IF: if
        - (: \(
      start: S
      productions:
        S:
          - F: if ( S ) then { else }
          - F: '" S "'
          - S S
          - ""
        T: []
    """.trimIndent()
    val y = mapFromYamlString(x)
    /*var g = */grammarFromMap(y)
    // println(g)
    // TODO: assertEqual(grammar, g)
  }
}
