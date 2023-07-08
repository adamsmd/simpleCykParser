package org.michaeldadams.simpleCykParser.grammar

import org.michaeldadams.simpleCykParser.util.toEqRegex
import kotlin.test.Test
import kotlin.test.assertEquals

// import kotlin.test.assertContentEquals
// import java.util.TreeMap
// import java.util.LinkedList

class GrammarTest {
  @Test fun testProperties(): Unit {
    // Terminal
    val terminal = Terminal("x")
    assertEquals("x", terminal.name)

    // Nonterminal
    val nonterminal = Nonterminal("A")
    assertEquals("A", nonterminal.name)

    // TerminalRule
    val terminalRegex = "x+".toRegex().toEqRegex()
    val terminalRule = TerminalRule(terminal, terminalRegex)
    assertEquals(terminal, terminalRule.terminal)
    assertEquals(terminalRegex, terminalRule.regex)

    // LexRules
    val whitespaceRegex = "\\s+".toRegex().toEqRegex()
    val lexRules = LexRules(whitespaceRegex, listOf(terminalRule))
    assertEquals(whitespaceRegex, lexRules.whitespace)
    assertEquals(listOf(terminalRule), lexRules.terminalRules)

    // Production
    val rhs = listOf("T" to terminal, "N" to nonterminal, null to terminal, null to nonterminal)
    val production = Production(nonterminal, "P", rhs)
    assertEquals(nonterminal, production.lhs)
    assertEquals("P", production.name)
    assertEquals(rhs, production.rhs)

    // ParseRules
    val productionMap = mapOf(nonterminal to setOf(production))
    val parseRules = ParseRules(nonterminal, productionMap)
    assertEquals(nonterminal, parseRules.start)
    assertEquals(productionMap, parseRules.productionMap)

    // Grammar
    val grammar = Grammar(lexRules, parseRules)
    assertEquals(lexRules, grammar.lexRules)
    assertEquals(parseRules, grammar.parseRules)
  }

  // TODO
  // @Test fun testUndefined(): Unit {
  //   val x = Terminal("x")
  //   val y = Terminal("y")
  //   val a = Nonterminal("a")
  //   val b = Nonterminal("b")
  //   val
  //   A -> A B A B A
  //   A -> x y x y x
  //   val grammar.undefinedSymbols()
  //   setOf(p1 to 1)
  //   val map = TreeMap(mapOf("B" to 2, "A" to 1, "C" to 3))

  //   assertContentEquals(
  //     sequenceOf("A" to 1, "B" to 2, "C" to 3),
  //     NavigableIterator(map).asSequence().map { it.key to it.value })

  //   assertContentEquals(
  //     sequenceOf("C" to 3, "B" to 2, "A" to 1),
  //     ReverseNavigableIterator(map).asSequence().map { it.key to it.value })
  // }

  // @Test fun testQueueIterator(): Unit {
  //   var results = emptyList<String>()

  //   val queue = LinkedList<String>()
  //   queue.add("A")
  //   queue.add("B")
  //   for (x in QueueIterator(queue)) {
  //     results += x
  //     if (x.length < 3) {
  //       queue.add(x + "A")
  //       queue.add(x + "B")
  //     }
  //   }

  //   assertContentEquals(
  //     listOf(
  //       "A", "B", "AA", "AB", "BA", "BB", "AAA", "AAB", "ABA", "ABB", "BAA", "BAB", "BBA", "BBB"),
  //     results)
  // }
}
