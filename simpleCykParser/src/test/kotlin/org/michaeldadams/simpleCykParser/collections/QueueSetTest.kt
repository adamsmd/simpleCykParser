package org.michaeldadams.simpleCykParser.collections

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import java.util.LinkedHashMap

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

  @Test fun testConcurrent2(): Unit {
    var result = emptyList<String>()

    val queue = LinkedHashMap<String, Int>()
    queue.put("A", 1)
    queue.put("B", 2)
    // TODO:
    queue.forEach { key, value ->
    // for ((key, value) in queue) {
      println("$key $value")
      result += key
      // if (key.length < 3) {
      //   queue.put(key + "A", 10 * value + 1)
      //   queue.put(key + "B", 10 * value + 2)
      // }
    }
    // for (x in queue) {
    //   result += x
    //   if (x.length < 3) {
    //     queue.add(x + "A")
    //     queue.add(x + "B")
    //   }
    // }

    // val expected = listOf(
    //   "A", "B", "AA", "AB", "BA", "BB", "AAA", "AAB", "ABA", "ABB", "BAA", "BAB", "BBA", "BBB"
    // )

    // assertContentEquals(expected, result)
  }

  @Test fun testUnsupportedOperations(): Unit {
    val queue = QueueSet<String>()
    queue.add("A")

    assertFailsWith(UnsupportedOperationException::class) { queue.iterator().remove() }
    assertFailsWith(UnsupportedOperationException::class) { queue.remove("A") }
    assertFailsWith(UnsupportedOperationException::class) { queue.removeAll(listOf("A")) }
    assertFailsWith(UnsupportedOperationException::class) { queue.clear() }
  }
}
