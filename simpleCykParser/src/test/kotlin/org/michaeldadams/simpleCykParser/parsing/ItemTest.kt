package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.RhsElement
import org.michaeldadams.simpleCykParser.grammar.Terminal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ItemTest {
  val lhs = Nonterminal("N")
  val t1 = Terminal("T1")
  val t2 = Terminal("T2")
  val rhs = Rhs(null, listOf(RhsElement(null, t1), RhsElement(null, t2)))

  @Test fun testConstruction(): Unit {
    assertFailsWith(IllegalArgumentException::class) { Item(lhs, rhs, -1) }
    Item(lhs, rhs, 0) // Expect no exception
    Item(lhs, rhs, 1) // Expect no exception
    Item(lhs, rhs, 2) // Expect no exception
    assertFailsWith(IllegalArgumentException::class) { Item(lhs, rhs, 3) }
  }

  @Test fun testConsume(): Unit {
    val i0 = Item(lhs, rhs, 0)

    val (s0, i1) = assertNotNull(i0.consume())
    assertEquals(t1, s0)
    assertEquals(Item(lhs, rhs, 1), i1)

    val (s1, i2) = assertNotNull(i1.consume())
    assertEquals(t2, s1)
    assertEquals(Item(lhs, rhs, 2), i2)

    assertNull(i2.consume())
  }
}
