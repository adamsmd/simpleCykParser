package org.michaeldadams.simpleCykParser.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class AutoMapTest {
  @Test fun testAutoMap(): Unit {
    var count = 0
    val map = autoMap<String, Int> { count += 1; count }
    map["A"] = 10
    assertEquals(10, map["A"])
    assertEquals(1, map["B"])
    assertEquals(1, map["B"]) // Once set, it stays
    assertEquals(2, map["C"])
    map["C"] = 20
    assertEquals(20, map["C"])
  }
}
