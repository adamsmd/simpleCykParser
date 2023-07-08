/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.initialUses
import org.michaeldadams.simpleCykParser.grammar.nullable

/**
 * TODO.
 *
 * @property parseRules TODO
 */
data class ProcessedParseRules(val parseRules: ParseRules) {
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
fun ParseRules.toProcessed(): ProcessedParseRules = ProcessedParseRules(this)
