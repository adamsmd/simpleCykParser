package org.michaeldadams.simpleCykParser.collections

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class QueueSetTest {
  @Test fun testNonConcurrent(): Unit {
    var result = emptyList<String>()

    val queue = QueueSet<String>()
    queue.add("A")
    queue.add("B")
    queue.add("B")
    queue.add("C")
    for (x in queue) {
      result += x
    }

    assertContentEquals(listOf("A", "B", "C"), result)
    assertFailsWith(NoSuchElementException::class) {
      val i = queue.iterator()
      i.next()
      i.next()
      i.next()
      i.next()
    }
  }

  @Test fun testConcurrent(): Unit {
    var result = emptyList<String>()

    val queue = QueueSet<String>()
    queue.add("A")
    queue.add("B")
    // TODO:
    for (x in queue) {
      result += x
      if (x.length < 3) {
        queue.add(x + "A")
        queue.add(x + "B")
      }
    }

    val expected = listOf(
      "A", "B", "AA", "AB", "BA", "BB", "AAA", "AAB", "ABA", "ABB", "BAA", "BAB", "BBA", "BBB"
    )

    assertContentEquals(expected, result)
  }

  @Test fun testUnsupportedOperations(): Unit {
    val queue = QueueSet<String>()
    queue.add("A")

    assertFailsWith(UnsupportedOperationException::class) { queue.iterator().remove() }
    assertFailsWith(UnsupportedOperationException::class) { queue.remove("A") }
    assertFailsWith(UnsupportedOperationException::class) { queue.clear() }
  }
}
