/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.initialUses
import org.michaeldadams.simpleCykParser.grammar.nullable

// TODO: rename to Parser

/**
 * TODO.
 *
 * @property parseRules TODO
 */
data class Parser(val parseRules: ParseRules) {
  /** TODO. */
  val nullable: Set<Production> = parseRules.nullable()

  /** TODO. */
  val initialUses: Map<Symbol, Set<Production>> = parseRules.initialUses()
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.toParser(): Parser = Parser(this)
