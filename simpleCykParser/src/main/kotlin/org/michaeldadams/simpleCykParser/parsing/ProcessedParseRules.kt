/** TODO. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.productionsUsing

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.initialUses(): Map<Symbol, Set<Production>> =
  this.productionMap.values.flatten()
    .filter { it.rhs.isNotEmpty() }
    .groupBy { it.rhs.first().second }
    .mapValues { it.value.toSet() }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.emptyProductions(): Set<Production> =
  this.productionMap.values.flatten().filter { it.rhs.isEmpty() }.toSet()

/**
 * TODO.
 *
 * TODO: Note that there are much more efficient algorithms for this.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.nullable(): Set<Production> {
  val uses: Map<Symbol, Set<Production>> = this.productionsUsing()
  // TODO: Note that productions serves as both a record (Set) and worklist (Queue)
  var productions: QueueSet<Production> = this.emptyProductions().toCollection(QueueSet())
  var nonterminals: Set<Nonterminal> = productions.map { it.lhs }.toSet()

  for (workitem in productions) { // Work through the queue until it is empty
    // For each use of workitems's nonterminal
    for (production in uses.getOrDefault(workitem.lhs, emptySet())) {
      // If rhs is only nullable nonterminals, then the production is nullable
      if (production.rhs.all { it.second is Nonterminal && it.second in nonterminals }) {
        productions += production // Enqueue the nullable production
        nonterminals += production.lhs // Record the nullable nonterminal
      }
    }
  }

  return productions.toSet()
}

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
