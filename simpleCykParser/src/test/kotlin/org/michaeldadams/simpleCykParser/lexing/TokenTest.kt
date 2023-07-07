package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.Terminal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class TokenTest {
  @Test fun testProperties(): Unit {
    val terminal = Terminal("x")

    val match = "x(y*)".toRegex().matchEntire("xyy")!!
    val groups = match.groups.toList()

    fun testToken(token: Token) {
      assertEquals(terminal, token.terminal)
      assertEquals(groups, token.groups)
      assertEquals(groups.first(), token.region)
    }

    testToken(Token(terminal, groups))
    testToken(match.toToken(terminal))
  }

  @Test fun testRequires(): Unit {
    val terminal = Terminal("x")
    assertFailsWith(IllegalArgumentException::class) { Token(terminal, listOf()) }
    assertFailsWith(IllegalArgumentException::class) { Token(terminal, listOf(null)) }
  }
}
