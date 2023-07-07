/** Implementation of a simple lexing / tokenization algorithm. */

package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.LexRules

/**
 * Perform lexing over some text.
 *
 * Stops at the end of [chars] or if no nonterminals match
 *
 * @param lexRules the rules to use for lexing
 * @param chars the text to lex
 * @return a pair of where the lexer stopped (exclusive) and the list of tokens lexed up to that
 * point
 */
fun lex(lexRules: LexRules, chars: CharSequence): Pair<Int, List<Token>> {
  val tokens: MutableList<Token> = mutableListOf() // result tokens to be returned

  var pos = 0 // The current position in [chars]
  while (true) {
    // Move [pos] to skip over whitespace
    pos = lexRules.whitespace.matchAt(chars, pos)?.let { it.range.endInclusive + 1 } ?: pos

    val token = lexRules.terminalRules
      // At the current position, try the regular expression for each [LexRule]
      .mapNotNull { it.regex.matchAt(chars, pos)?.toToken(it.terminal) }
      // Take the longest match.  In case of a tie, use the first one to occur in [rules].
      .maxByOrNull { it.region.range.endInclusive }
      // If no matches, exit the loop
      ?: break

    tokens += token // Add the token to the results
    pos = token.region.range.endInclusive + 1 // Move the position past the end of the token
  }

  return Pair(pos, tokens)
}
