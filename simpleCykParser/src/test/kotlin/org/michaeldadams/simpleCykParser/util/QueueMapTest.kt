package org.michaeldadams.simpleCykParser.util

import kotlin.test.Test
import kotlin.test.assertEquals

class QueueMapTest {
  @Test fun testQueueMap(): Unit {
    var count = 0
    val map: QueueMap<String, String> = queueMap { it + count++ }
    assertEquals("A0", map["A"])
    assertEquals("B1", map["B"])
    assertEquals("B1", map["B"]) // Once set, it stays
    assertEquals("C2", map["C"]) // New entries re-invoke the default-value function
  }
}

// TODO: .clear()
// TODO: .remove()
// TODO: .keys
