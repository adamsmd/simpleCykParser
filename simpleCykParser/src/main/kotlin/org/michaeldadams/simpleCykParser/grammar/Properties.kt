/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.grammar

import org.michaeldadams.simpleCykParser.util.QueueSet

// TODO: check @throws

/**
 * TODO: Compute the productions using each nonterminal.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.productionsUsing(): Map<Symbol, Set<Production>> =
  this.productionMap.values
    .flatMap { productions ->
      productions.flatMap { production ->
        production.rhs.map { it.second to production }
      }
    }
    .groupBy { it.first }
    .mapValues { entry -> entry.value.map { it.second }.toSet() }

fun ParseRules.usedSymbols(): Set<Symbol> =
  this.productionMap.values.flatten().flatMap { it.rhs }.map { it.second }.toSet() + this.start

fun LexRules.definedTerminals(): Set<Terminal> = this.terminalRules.map { it.terminal }.toSet()

fun ParseRules.definedNonterminals(): Set<Nonterminal> = this.productionMap.keys

fun Grammar.definedSymbols(): Set<Symbol> =
  this.lexRules.definedTerminals() + this.parseRules.definedNonterminals()

/**
 * Find productions that use undefined symbols.
 *
 * In a well-formed grammar, this function will return the empty set.
 *
 * @receiver TODO
 * @return pairs of the productions using undefined symbols and the position
 * of the undefined symbol in the [rhs] of that production
 */
fun Grammar.undefinedSymbols(): Set<Pair<Production, Int>> =
  this.definedSymbols().let { symbols ->
    this.parseRules.productionMap.values.flatten().flatMap { production ->
      production.rhs.mapIndexedNotNull { i, c ->
        if (c.second in symbols) null else Pair(production, i)
      }
    }.toSet()
  }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.productionlessNonterminals(): Set<Nonterminal> =
  this.productionMap.entries.filter { it.value.isEmpty() }.map { it.key }.toSet()

/**
 * TODO.
 *
 * Recursive version of productionless
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.emptyNonterminals(): Set<Nonterminal> {
  val uses: Map<Symbol, Set<Production>> = this.productionsUsing()
  val empty = this.productionlessNonterminals().toCollection(QueueSet())

  for (nonterminal in empty) {
    for (usingProduction in uses.getOrDefault(nonterminal, emptySet())) {
      // TODO: remove !!
      val lhsIsEmpty = this.productionMap[usingProduction.lhs]!!.all { production ->
        production.rhs.any { it.second in empty }
      }
      if (lhsIsEmpty) empty += usingProduction.lhs
    }
  }

  return empty.toSet()
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.unusedSymbols(): Set<Symbol> = this.definedSymbols() - this.parseRules.usedSymbols()

fun ParseRules.reachableSymbols(): Set<Symbol> {
  val reachable = QueueSet<Symbol>()

  reachable += this.start
  for (symbol in reachable) {
    if (symbol is Nonterminal) {
      for (production in this.productionMap[symbol]!!) {
        for (component in production.rhs) {
          reachable += component.second
        }
      }
    }
  }

  return reachable.toSet()
}

/**
 * TODO.
 *
 * Recursive version of unused
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.unreachableSymbols(emptyNonterminals: Set<Nonterminal>): Set<Symbol> =
  this.definedSymbols() - this.parseRules.reachableSymbols()
