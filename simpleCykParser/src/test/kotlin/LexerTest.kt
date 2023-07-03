import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {
  @Test fun test1(): Unit {
    val x = """
      whitespace: "\\s+"
      terminals:
        - A: "A"
        - B: "B"
        - C: "[A-Z]+"
    """.trimIndent()
    val y = parseYaml(x)
    val l = lexRulesFromYamlMap(y)
    assertEquals(
      Pair(9, listOf(Token("A", 1, 1), Token("B", 3, 3), Token("C", 6, 7))),
      Lexer.lex(l, " A B  AA "))
  }
}
