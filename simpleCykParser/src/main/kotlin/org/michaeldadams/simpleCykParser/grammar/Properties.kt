/**
 * TODO.
 *
 * TODO: Not everything here is actually used for parsing.
 */

package org.michaeldadams.simpleCykParser.grammar

import org.michaeldadams.simpleCykParser.util.QueueMap
import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.util.queueMap

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

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.usedSymbols(): Set<Symbol> =
  this.productionMap.values.flatten().flatMap { it.rhs }.map { it.second }.toSet() + this.start

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun LexRules.definedTerminals(): Set<Terminal> = this.terminalRules.map { it.terminal }.toSet()

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.definedNonterminals(): Set<Nonterminal> = this.productionMap.keys

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
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
      production.rhs.mapIndexedNotNull { i, component ->
        if (component.second in symbols) null else Pair(production, i)
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

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.reachableSymbols(): Set<Symbol> {
  val reachable: QueueSet<Symbol> = QueueSet()

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
fun Grammar.unreachableSymbols(): Set<Symbol> =
  this.definedSymbols() - this.parseRules.reachableSymbols()

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
fun ParseRules.epsilonProductions(): Set<Production> =
  this.productionMap.values.flatten().filter { it.rhs.isEmpty() }.toSet()

/**
 * TODO.
 *
 * TODO: Note that there are much more efficient algorithms for this.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.nullableProductions(): Set<Production> {
  val uses: Map<Symbol, Set<Production>> = this.productionsUsing()
  // TODO: Note that productions serves as both a record (Set) and worklist (Queue)
  var productions: QueueSet<Production> = this.epsilonProductions().toCollection(QueueSet())
  var nonterminals: Set<Nonterminal> = productions.map { it.lhs }.toSet()

  // For each item in the queue until it is empty
  for (workitem in productions) {
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
 */
fun ParseRules.nullableSymbols(): Set<Symbol> = this.nullableProductions().map { it.lhs }.toSet()

/**
 * TODO.
 */
fun ParseRules.partiallyNullable(): Map<Production, Set<Int>> {
  val nullable: Set<Symbol?> = this.nullableProductions().map { it.lhs }.toSet()

  val result: QueueMap<Production, QueueSet<Int>> = queueMap { QueueSet() }

  for (production in this.productionMap.values.flatten()) {
    var consumed = 0
    do {
      result[production].add(consumed)
    } while (production.rhs.getOrNull(consumed++)?.second in nullable)
  }
  return result
}
