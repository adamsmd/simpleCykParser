/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.initialUses
import org.michaeldadams.simpleCykParser.grammar.nullableSymbols
import org.michaeldadams.simpleCykParser.grammar.partiallyNullable

// TODO: rename to Parser

/**
 * TODO.
 *
 * @property parseRules TODO
 */
data class Parser(val parseRules: ParseRules) {
  /** TODO. */
  val nullableSymbols: Set<Symbol> = parseRules.nullableSymbols()

  /** TODO. */
  val initialUses: Map<Symbol, Set<Production>> = parseRules.initialUses()

  /** TODO. */
  val partiallyNullable: Map<Production, Set<Int>> = parseRules.partiallyNullable()
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.toParser(): Parser = Parser(this)
