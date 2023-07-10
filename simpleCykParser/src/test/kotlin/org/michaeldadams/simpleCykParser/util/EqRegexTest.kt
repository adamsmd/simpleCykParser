package org.michaeldadams.simpleCykParser.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EqRegexTest {
  @Test fun testProperties(): Unit {
    val pattern = "x+"
    val option = RegexOption.IGNORE_CASE
    val options = setOf(option)

    fun test(eqRegex: EqRegex, options: Set<RegexOption>): Unit {
      assertEquals(pattern, eqRegex.pattern)
      assertEquals(options, eqRegex.options)
      assertEquals(pattern, eqRegex.regex.pattern)
      assertEquals(options, eqRegex.regex.options)
    }

    test(EqRegex(pattern, options), options)
    test(EqRegex(pattern, option), setOf(option))
    test(EqRegex(pattern), setOf())
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
