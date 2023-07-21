package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.Terminal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TokenTest {
  @Test fun testConstruction(): Unit {
    val terminal = Terminal("x")

    val match = "x(y*)".toRegex().matchEntire("xyy")!!
    val groups = match.groups.toList() // TODO: direct construction of MatchGroup

    fun test(token: Token): Unit {
      assertEquals(terminal, token.terminal)
      assertEquals(groups, token.groups)
      assertEquals(groups.first(), token.region)
    }

    test(Token(terminal, groups))
    test(match.toToken(terminal))
  }

  @Test fun testRequires(): Unit {
    val terminal = Terminal("x")
    assertFailsWith(IllegalArgumentException::class) { Token(terminal, listOf()) }
    assertFailsWith(IllegalArgumentException::class) { Token(terminal, listOf(null)) }
  }
}
