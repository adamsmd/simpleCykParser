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

    // TODO: test key removal
    // assertFailsWith(UnsupportedOperationException::class) { queue.iterator().remove() }
    // assertFailsWith(UnsupportedOperationException::class) { queue.remove("A") }
    // assertFailsWith(UnsupportedOperationException::class) { queue.removeAll(listOf("A")) }
    // assertFailsWith(UnsupportedOperationException::class) { queue.clear() }

    assertContentEquals(listOf("A", "B", "C"), result)
    assertFailsWith(NoSuchElementException::class) {
      val i = map.keys.iterator()
      i.next()
      i.next()
      i.next()
      i.next()
    }
  }
}
