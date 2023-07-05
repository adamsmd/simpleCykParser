package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.collections.QueueIterator
import org.michaeldadams.simpleCykParser.collections.autoMap
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import java.util.LinkedList
import java.util.Queue

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
  var productions: Set<Production> = this.emptyProductions()
  var nonterminals: Set<Nonterminal> = productions.map { it.lhs }.toSet()
  var worklist: Queue<Production> = LinkedList(productions)

  for (workitem in QueueIterator(worklist)) { // Work through the queue until it is empty
    if (workitem !in productions) { // Skip if already nullable
      // For each use of workitems's nonterminal
      for (production in uses.getOrDefault(workitem.lhs, emptySet())) {
        // If rhs is only nullable nonterminals, then the production is nullable
        if (production.rhs.all { it is Nonterminal && it in nonterminals }) {
          productions += production // Record the nullable production
          nonterminals += production.lhs // Record the nullable nonterminal
          worklist += production // Enqeue the production since it may cause more nullables
        }
      }
    }
  }

  return productions
}
