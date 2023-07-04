package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.collections.DefHashMap
import org.michaeldadams.simpleCykParser.collections.DefMap
import org.michaeldadams.simpleCykParser.collections.QueueIterator
import org.michaeldadams.simpleCykParser.collections.defHashMap
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
fun ParseRules.nullable(): Set<Nonterminal> {
  val uses: Map<Symbol, Set<Production>> = this.productionsUsing()
  var nullable: Set<Nonterminal> = this.emptyProductions().map { it.lhs }.toSet()
  var queue: Queue<Nonterminal> = LinkedList(nullable)

  for (nt in QueueIterator(queue)) { // Work through the queue until it is empty
    if (nt !in nullable) { // Skip already nullable nonterminals
      for (production in uses.getOrDefault(nt, emptySet())) { // For each production using nt
        // If everything in the rhs is a nullable nonterminal, then the lhs is nullable
        if (production.rhs.all { it is Nonterminal && it in nullable }) {
          nullable += production.lhs // Record that the lhs is nullable
          queue += production.lhs // The lhs being nullable may more nullables, so enqueue it for processing
        }
      }
    }
  }

  return nullable
}
