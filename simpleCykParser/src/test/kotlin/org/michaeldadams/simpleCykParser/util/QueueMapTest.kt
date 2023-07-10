package org.michaeldadams.simpleCykParser.util

import kotlin.test.Test
import kotlin.test.assertEquals

class QueueMapTest {
  @Test fun testQueueMap(): Unit {
    var count = 0
    val map: QueueMap<String, Int> = queueMap { count++ }
    assertEquals(0, map["A"])
    assertEquals(1, map["B"])
    assertEquals(1, map["B"]) // Once set, it stays
    assertEquals(2, map["C"]) // New entries re-invoke the default-value function
  }
}
