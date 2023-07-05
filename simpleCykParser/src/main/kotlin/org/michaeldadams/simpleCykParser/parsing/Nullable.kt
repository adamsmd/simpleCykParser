package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.collections.QueueSet
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol

/** Compute the productions using each nonterminal. */
fun ParseRules.productionsUsing(): Map<Symbol, Set<Production>> =
  this.productions.values.flatMap { productions ->
    productions.flatMap { production ->
      production.rhs.map { it to production }
    }
  }.groupBy { it.first }
    .mapValues { entry -> entry.value.map { it.second }.toSet() }

fun ParseRules.emptyProductions(): Set<Production> =
  this.productions.values.flatMap { productions -> productions.filter { it.rhs.isEmpty() } }.toSet()

// Note that there are much more efficient algorithms for this
fun ParseRules.nullable(): Set<Production> {
  val uses: Map<Symbol, Set<Production>> = this.productionsUsing()
  // TODO: Note that productions serves as both a record (Set) and worklist (Queue)
  var productions: QueueSet<Production> = this.emptyProductions().toCollection(QueueSet())
  var nonterminals: Set<Nonterminal> = productions.map { it.lhs }.toSet()

  for (workitem in productions) { // Work through the queue until it is empty
    // For each use of workitems's nonterminal
    for (production in uses.getOrDefault(workitem.lhs, emptySet())) {
      // If rhs is only nullable nonterminals, then the production is nullable
      if (production.rhs.all { it is Nonterminal && it in nonterminals }) {
        productions += production // Enqueue the nullable production
        nonterminals += production.lhs // Record the nullable nonterminal
      }
    }
  }

  return productions.toSet()
}
