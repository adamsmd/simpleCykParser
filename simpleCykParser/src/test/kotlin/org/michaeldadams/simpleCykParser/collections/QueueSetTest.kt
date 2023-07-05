package org.michaeldadams.simpleCykParser.collections

import kotlin.test.Test
import kotlin.test.assertContentEquals

class QueueSetTest {
  @Test fun testQueueSet(): Unit {
    var results = emptyList<String>()
    
    val queue = QueueSet<String>()
    queue.add("A")
    queue.add("B")
    // TODO:
    // for (x in queue) {
    //   results += x
    //   if (x.length < 3) {
    //     queue.add(x + "A")
    //     queue.add(x + "B")
    //   }
    // }

    // val expected = listOf(
    //   "A", "B", "AA", "AB", "BA", "BB", "AAA", "AAB", "ABA", "ABB", "BAA", "BAB", "BBA", "BBB"
    // )

    // assertContentEquals(expected, results)
  }
}
