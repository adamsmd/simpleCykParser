package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.collections.DefHashMap
import org.michaeldadams.simpleCykParser.collections.QueueIterator
import org.michaeldadams.simpleCykParser.collections.defHashMap
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import java.util.LinkedList
import java.util.Queue

fun nullable(parseRules: ParseRules): Set<Symbol> {
  // productionsUsing[nt][lhs].contains(p) iff p.lhs == lhs and p.rhs.contains(nt)
  // Production p in lhs uses nt
  val productionsUsing: DefHashMap<Nonterminal, DefHashMap<Nonterminal, MutableSet<Production>>> =
    defHashMap { defHashMap { mutableSetOf() } }

  // nonterminalsUsedBy[p].contains(nt) iff p.rhs.contains(nt) and nt is not nullable
  // nt is in p.rhs and is not (yet) nullable
  // A production is null if all these symbols are null
  val nonterminalsUsedBy: DefHashMap<Production, MutableSet<Nonterminal>> =
    defHashMap({ mutableSetOf() })

  var nullable: Set<Nonterminal> = emptySet()
  var queue: Queue<Nonterminal> = LinkedList()

  // Initialize productionsUsing, nonterminalsUsedBy, nullable, and queue
  for ((lhs, productions) in parseRules.productions.entries) {
    if (productions.any { it.rhs.isEmpty() }) {
      // If any production for lhs is empty, then lhs is trivially nullable
      nullable += lhs
      queue += lhs
    } else {
      // If lhs is not trivially nullable, populate productionsUsing and nonterminalsUsedBy
      for (production in productions) {
        try {
          for (nt in production.rhs.map { it as Nonterminal }) {
            productionsUsing[nt][lhs] += production
            nonterminalsUsedBy[production] += nt
          }
        } catch (_: ClassCastException) {
          // The rhs contained a Terminal, so the production will never be
          // nullable.  Thus we bail out of working on this production by
          // doing nothing in this 'catch' block.
        }
      }
    }
  }

  // Work through the queue until it is empty
  for (nt in QueueIterator(queue)) {
    if (nt !in nullable) { // Skip already nullable nonterminals
      // Look at the productions that use nt
      for ((lhs, productions) in productionsUsing[nt].entries) {
        if (lhs !in nullable) { // Skip already nullable nonterminals
          for (production in productions) {
            val ntsInRhs = nonterminalsUsedBy[production]
            // Remove nt from the nonterminals preventing the production from being nullable
            ntsInRhs -= nt
            // If all nonterminals have been removed, then the production is nullable
            if (ntsInRhs.isEmpty()) {
              nullable += lhs
              queue += lhs
            }
          }
        }
      }
    }
  }

  return nullable
}
