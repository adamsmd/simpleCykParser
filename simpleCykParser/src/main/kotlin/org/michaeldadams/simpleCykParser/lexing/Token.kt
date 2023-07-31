/** Implementation of a simple lexing / tokenization algorithm. */

package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.util.Generated as Gen

/**
 * A token produced by lexing.
 *
 * In [groups], index 0 corresponds to the entire match.  The remaining elements
 * are capture groups of the token's regular expression and are indexed from 1.
 *
 * An capture group's element can be `null` if there is no match captured by
 * that group.
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
data class Token(@get:Gen val terminal: Terminal, @get:Gen val groups: List<MatchGroup?>) {
  /** The [MatchGroup] for the entire region matched by the token. */
  @Suppress("CUSTOM_GETTERS_SETTERS")
  val region: MatchGroup get() = groups.first()!!

  init {
    require(groups.isNotEmpty()) { "Groups list must not be empty" }
    requireNotNull(groups.first()) { "First element of groups list must not be null" }
  }
}

/**
 * Construct a token from a [MatchResult] and a terminal.
 *
 * @receiver the [MatchResult] to convert the token from.
 * @param terminal the terminal that the token is for
 * @return the token constructed for the [terminal] and the [MatchResult]
 */
fun MatchResult.toToken(terminal: Terminal): Token = Token(terminal, this.groups.toList())
