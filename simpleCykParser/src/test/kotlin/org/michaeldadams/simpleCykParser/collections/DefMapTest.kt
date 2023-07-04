package org.michaeldadams.simpleCykParser.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class DefMapTest {
  fun <M : MutableMap<String, Int>> testDefMap(makeMap: (() -> Int) -> DefMap<String, Int, M>): Unit {
    var count = 0
    val map = makeMap { count += 1; count }
    map["A"] = 10
    assertEquals(10, map["A"])
    assertEquals(1, map["B"])
    assertEquals(1, map["B"]) // Once set, it stays
    assertEquals(2, map["C"])
    map["C"] = 20
    assertEquals(20, map["C"])
  }

  @Test fun testDefHashMap(): Unit = testDefMap(::defHashMap)

  @Test fun testDefTreeMap(): Unit = testDefMap(::defTreeMap)
}
