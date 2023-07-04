package org.michaeldadams.simpleCykParser.collections.iterators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals
import java.util.TreeMap
import java.util.LinkedList

class GrammarTest {
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
  //   var results = listOf<String>()

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
