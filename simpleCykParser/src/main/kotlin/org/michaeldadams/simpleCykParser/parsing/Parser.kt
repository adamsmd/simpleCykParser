/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.ProductionMap
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.initialUses
import org.michaeldadams.simpleCykParser.grammar.nullable
import org.michaeldadams.simpleCykParser.grammar.nullablePrefixes

/**
 * TODO.
 *
 * @property parseRules TODO
 */
data class Parser(val parseRules: ParseRules) {
  /** TODO: Populates chart.entries. */
  // val nullablePartials: Set<Item> = parseRules.nullablePartials()

  /** TODO: Populates Chart._symbolEnds. */
  // val nullable: Set<Symbol> = parseRules.nullable().keys

  /** TODO: Used by Chart.add(). */
  // TODO: rename to itemInitials?
  // val initialItems: Map<Symbol?, Set<Item>> = parseRules.initialItems()
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.toParser(): Parser = Parser(this)
