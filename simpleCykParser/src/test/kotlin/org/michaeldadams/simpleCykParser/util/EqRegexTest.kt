package org.michaeldadams.simpleCykParser.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EqRegexTest {
  @Test fun testProperties(): Unit {
    val pattern = "x+"
    val options = setOf(RegexOption.IGNORE_CASE)
    val eqRegex = EqRegex(pattern, options)

    assertEquals(pattern, eqRegex.pattern)
    assertEquals(options, eqRegex.options)
    assertEquals(pattern, eqRegex.regex.pattern)
    assertEquals(options, eqRegex.regex.options)
  }

  @Test fun testEquality(): Unit {
    val options = setOf(RegexOption.IGNORE_CASE)

    assertEquals(EqRegex("x", setOf()), EqRegex("x", setOf()))
    assertNotEquals(EqRegex("x", setOf()), EqRegex("y", setOf()))
    assertNotEquals(EqRegex("x", setOf()), EqRegex("x", options))

    assertEquals(EqRegex("x", options), EqRegex("x", options))
    assertNotEquals(EqRegex("x", options), EqRegex("y", options))
    assertNotEquals(EqRegex("x", options), EqRegex("x", setOf()))
  }

  @Test fun testToEqRegex(): Unit {
    val pattern = "x+"
    val options = setOf(RegexOption.IGNORE_CASE)

    assertEquals(EqRegex(pattern, setOf()), pattern.toRegex().toEqRegex())
    assertEquals(EqRegex(pattern, options), pattern.toRegex(options).toEqRegex())
  }
}
