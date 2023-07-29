package org.michaeldadams.simpleCykParser.main

import com.github.ajalt.clikt.testing.test
import org.michaeldadams.simpleCykParser.BuildInformation
import kotlin.test.Test
import kotlin.test.assertEquals

class MainTest {
  @Test fun test(): Unit {
    // TODO
    val result = Main().test("--version")
    assertEquals("simpleCykParser version ${BuildInformation.VERSION}\n", result.stdout)

    // TODO: val result = CheckGrammar().test("--foo", stdin = "...")
  }
}
