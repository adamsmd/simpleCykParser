/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.ProductionMap
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.initialUses
import org.michaeldadams.simpleCykParser.grammar.nullable
import org.michaeldadams.simpleCykParser.grammar.partiallyNullable

/**
 * TODO.
 *
 * @property parseRules TODO
 */
data class Parser(val parseRules: ParseRules) {
  /** TODO: Populates chart.entries. */
  val partiallyNullable: Map<Nonterminal, Map<Rhs, Set<Int>>> = parseRules.partiallyNullable()

  /** TODO: Populates Chart._symbolEnds. */
  val nullable: Set<Symbol> = parseRules.nullable().keys

  /** TODO: Used by Chart.add(). */
  val initialUses: Map<Symbol?, ProductionMap> = parseRules.initialUses()
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.toParser(): Parser = Parser(this)
