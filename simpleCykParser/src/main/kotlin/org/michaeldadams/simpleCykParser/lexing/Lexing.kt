package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.*

data class Token(val terminal: Terminal, val range: IntRange) {
  constructor(name: String, range: IntRange) : this(Terminal(name), range)
  constructor(terminal: Terminal, start: Int, endInclusive: Int) : this(terminal, IntRange(start, endInclusive))
  constructor(name: String, start: Int, endInclusive: Int) : this(Terminal(name), IntRange(start, endInclusive))
}

object Lexer {
  fun lex(lexerRules: LexRules, input: CharSequence): Pair<Int, List<Token>> {
    val tokens: MutableList<Token> = mutableListOf()

    var index = 0
    while (true) {
      index =
        lexerRules.whitespace.matchAt(input, index)?.let { it.range.endInclusive + 1 } ?: index

      val token = lexerRules.lexRules
        .mapNotNull { rule -> rule.regex.matchAt(input, index)?.let { Token(rule.terminal, it.range) } }
        .maxByOrNull { it.range.endInclusive }
        ?: break
      tokens += token

      index = token.range.endInclusive + 1
    }

    return Pair(index, tokens)
  }
}
