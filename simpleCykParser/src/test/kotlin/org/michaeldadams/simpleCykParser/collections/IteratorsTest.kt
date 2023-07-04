package org.michaeldadams.simpleCykParser.collections

import java.util.LinkedList
import java.util.TreeMap
import kotlin.test.Test
import kotlin.test.assertContentEquals

class IteratorsTest {
  @Test fun testNavigableIterator(): Unit {
    val map = TreeMap(mapOf("B" to 2, "A" to 1, "C" to 3))

    assertContentEquals(
      sequenceOf("A" to 1, "B" to 2, "C" to 3),
      NavigableIterator(map).asSequence().map { it.key to it.value }
    )

    assertContentEquals(
      sequenceOf("C" to 3, "B" to 2, "A" to 1),
      ReverseNavigableIterator(map).asSequence().map { it.key to it.value }
    )
  }

  @Test fun testQueueIterator(): Unit {
    var results = emptyList<String>()

    val queue = LinkedList<String>()
    queue.add("A")
    queue.add("B")
    for (x in QueueIterator(queue)) {
      results += x
      if (x.length < 3) {
        queue.add(x + "A")
        queue.add(x + "B")
      }
    }

    val expected = listOf(
      "A", "B", "AA", "AB", "BA", "BB", "AAA", "AAB", "ABA", "ABB", "BAA", "BAB", "BBA", "BBB"
    )

    assertContentEquals(expected, results)
  }
}
