package org.michaeldadams.simpleCykParser.util

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QueueMapTest {
  @Test fun testQueueMap(): Unit {
    var count = 0
    val map: QueueMap<String, String> = queueMap { it + count++ }
    assertEquals("A0", map["A"])
    assertEquals("B1", map["B"])
    assertEquals("B1", map["B"]) // Once set, it stays
    assertEquals("C2", map["C"]) // New entries re-invoke the default-value function
  }

  @Test fun testKeysNonConcurrent(): Unit {
    var result: List<String> = emptyList()

    val map: QueueMap<String, Int> = queueMap { 0 }
    map["A"]
    map["B"]
    map["B"]
    map["C"]
    for (x in map.keys) {
      result += x
    }

    assertContentEquals(listOf("A", "B", "C"), result)
    assertFailsWith(NoSuchElementException::class) {
      val i = map.keys.iterator()
      i.next()
      i.next()
      i.next()
      i.next()
    }
  }

  @Test fun testKeysConcurrent(): Unit {
    var result: List<String> = emptyList()

    val map: QueueMap<String, Int> = queueMap() { 0 }
    map["A"]
    map["B"]
    for (x in map.keys) {
      result += x
      if (x.length < 3) {
        map[x + "A"]
        map[x + "B"]
      }
    }

    val expected = listOf(
      "A", "B", "AA", "AB", "BA", "BB", "AAA", "AAB", "ABA", "ABB", "BAA", "BAB", "BBA", "BBB",
    )

    assertContentEquals(expected, result)
  }
}
