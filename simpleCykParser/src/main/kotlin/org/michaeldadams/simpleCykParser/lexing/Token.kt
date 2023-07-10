/** Implementation of a simple lexing / tokenization algorithm. */

package org.michaeldadams.simpleCykParser.lexing

import org.michaeldadams.simpleCykParser.grammar.Terminal

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
  /** The [MatchGroup] for the entire region matched by the token. */
  @Suppress("CUSTOM_GETTERS_SETTERS")
  val region: MatchGroup get() = groups.first()!!

  init {
    require(groups.isNotEmpty()) { "Groups list must not be empty" }
    requireNotNull(groups.first()) { "First element of groups list must not be null" }
  }
}

// TODO: List vs Collection or Sequence?

/**
 * Construct a [Token] from a [MatchResult] and a [Token].
 *
 * @receiver TODO
 * @param terminal the terminal that the token is for
 * @return the token constructed for the [terminal] and the [MatchResult]
 */
fun MatchResult.toToken(terminal: Terminal): Token = Token(terminal, this.groups.toList())
