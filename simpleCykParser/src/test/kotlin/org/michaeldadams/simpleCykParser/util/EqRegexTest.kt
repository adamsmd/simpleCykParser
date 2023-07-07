package org.michaeldadams.simpleCykParser.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EqRegexTest {
  @Test fun testProperties(): Unit {
    val pattern = "x+"
    val option = RegexOption.IGNORE_CASE
    val options = setOf(option)

    run {
      val eqRegex = EqRegex(pattern, options)
      assertEquals(pattern, eqRegex.pattern)
      assertEquals(options, eqRegex.options)
    }

    run {
      val eqRegex = EqRegex(pattern, option)
      assertEquals(pattern, eqRegex.pattern)
      assertEquals(setOf(option), eqRegex.options)
    }

    run {
      val eqRegex = EqRegex(pattern)
      assertEquals(pattern, eqRegex.pattern)
      assertEquals(setOf(), eqRegex.options)
    }
  }

  @Test fun testEquality(): Unit {
    val option = RegexOption.IGNORE_CASE

    assertEquals("x".toRegex().toEqRegex(), "x".toRegex().toEqRegex())
    assertNotEquals("x".toRegex().toEqRegex(), "y".toRegex().toEqRegex())

    assertEquals("x".toRegex(option).toEqRegex(), "x".toRegex(option).toEqRegex())
    assertNotEquals("x".toRegex(option).toEqRegex(), "x".toRegex().toEqRegex())
    assertNotEquals("x".toRegex(option).toEqRegex(), "y".toRegex(option).toEqRegex())
  }
}
