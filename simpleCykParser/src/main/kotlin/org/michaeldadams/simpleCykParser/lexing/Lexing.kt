/** Implementation of a simple lexing / tokenization algorithm. */

package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.LexRules

/**
 * A token produced by lexing.
 *
 * In [groups], index 0 corresponds to the entire match.  The remaining elements
 * are capture groups of the token's regular expression and are indexed from 1.
 *
 * An capture group's element can be `null` if there isno match captured by that
 * group.
 *
 * The [groups] list must not be empty and the element at index 0 must not be
 * `null`.
 *
 * Note that we do not use [MatchResult] or [MatchGroupCollection] because
 * they have no easily accessable constructor (e.g., for building tests).
 *
 * @property terminal the terminal for this token
 * @property groups the capture groups from the token's regular expression
 */
data class Token(val terminal: Terminal, val groups: List<MatchGroup?>) {
  init {
    require(groups.isNotEmpty()) { "Groups list must not be empty" }
    requireNotNull(groups.first()) { "First element of groups list must not be null" }
  }

  /** The [MatchGroup] for the entire region matched by the token. */
  val region: MatchGroup get() = groups.first()!!
}

/**
 * Construct a [Token] from a [MatchResult] and a [Token].
 *
 * @param terminal the terminal that the token is for
 * @return the token constructed for the [terminal] and the [MatchResult]
 */
fun MatchResult.toToken(terminal: Terminal): Token = Token(terminal, this.groups.map { it })

/**
 * Perform lexing over some text.
 *
 * Stops at the end of [chars] or if no nonterminals match
 *
 * @param lexerRules the rules to use for lexing
 * @param chars the text to lex
 * @return a pair of where the lexer stopped (exclusive) and the list of tokens lexed up to that
 * point
 */
fun lex(lexerRules: LexRules, chars: CharSequence): Pair<Int, List<Token>> {
  val tokens: MutableList<Token> = mutableListOf() // result tokens to be returned

  var pos = 0 // The current position in [chars]
  while (true) {
    // Move [pos] to skip over whitespace
    pos = lexerRules.whitespace.matchAt(chars, pos)?.let { it.range.endInclusive + 1 } ?: pos

    val token = lexerRules.lexRules
      // At the current position, try the regular expression for each [LexRule]
      .mapNotNull { it.regex.matchAt(chars, pos)?.toToken(it.terminal) }
      // Take the longest match.  In case of a tie, use the first one to occur in [lexRules].
      .maxByOrNull { it.region.range.endInclusive }
      // If no matches, exit the loop
      ?: break

    tokens += token // Add the token to the results
    pos = token.region.range.endInclusive + 1 // Move the position past the end of the token
  }

  return Pair(pos, tokens)
}
