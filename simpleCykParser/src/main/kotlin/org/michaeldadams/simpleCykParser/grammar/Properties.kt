/**
 * TODO.
 *
 * TODO: Not everything here is actually used for parsing.
 */

package org.michaeldadams.simpleCykParser.grammar

import org.michaeldadams.simpleCykParser.util.QueueMap
import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.util.fromSetsMap
import org.michaeldadams.simpleCykParser.util.queueMap
import org.michaeldadams.simpleCykParser.util.toSetsMap

// TODO: check @throws

/**
 * TODO: Compute the productions using each symbol.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.productionsUsing(): Map<Symbol, ProductionMap> =
  this.productionMap.fromSetsMap()
    .flatMap { (lhs, rhs) -> rhs.parts.map { it.second to (lhs to rhs) } }
    .toSetsMap()
    .mapValues { it.value.toSetsMap() }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.nonterminalsUsing(): Map<Symbol, Set<Nonterminal>> =
  this.productionsUsing().mapValues { it.value.keys }

/**
 * Get all the symbols used in the right-hand sides ([Rhs]) of a [ParseRules].
 *
 * @receiver the parse rules to get the used symbols from
 * @return all the symbols used in the right-hand sides of the given parse rules
 */
fun ParseRules.usedSymbols(): Set<Symbol> =
  this.productionMap.values.flatten().flatMap { it.parts }.map { it.second }.toSet() + this.start

/**
 * Get the terminals defined by a [LexRules].
 *
 * @receiver the lexing rules to get the defined terminals from
 * @return all the terminals defined in the given lexing rules
 */
fun LexRules.definedTerminals(): Set<Terminal> = this.terminalRules.map { it.terminal }.toSet()

/**
 * Get the nonterminals defined by a [ParseRules].
 *
 * @receiver the parse rules to get the defined nonterminals from
 * @return all the nonterminals defined in the given parse rules
 */
fun ParseRules.definedNonterminals(): Set<Nonterminal> = this.productionMap.keys

/**
 * Get the symbols (terminals and nonterminals) defined by a [Grammar].
 *
 * @receiver the grammar to get the defined symbols from
 * @return all the symbols defined in the given grammar
 */
fun Grammar.definedSymbols(): Set<Symbol> =
  this.lexRules.definedTerminals() + this.parseRules.definedNonterminals()

/**
 * Find productions that use undefined symbols.
 *
 * In a well-formed grammar, this function will return the empty set.
 *
 * @receiver the grammar in which to get the undefined symbols
 * @return triples of the left-hand size ([Nonterminal]), right-hand side
 *   ([Rhs]) and the position ([Int]) of the undefined symbol in the right-hand
 *   side
 */
fun Grammar.undefinedSymbols(): Set<Triple<Nonterminal, Rhs, Int>> {
  val symbols = this.definedSymbols()
  return this.parseRules.productionMap.fromSetsMap().flatMap { (lhs, rhs) ->
    rhs.parts.mapIndexedNotNull { i, part ->
      if (part.second in symbols) null else Triple(lhs, rhs, i)
    }
  }.toSet()
}

/**
 * Get the nonterminals that have no production.
 *
 * @receiver the parse rules from which to get the productionless nonterminals
 * @return the nonterminals that have no production in the parse rules
 */
fun ParseRules.productionlessNonterminals(): Set<Nonterminal> =
  this.productionMap.entries.filter { it.value.isEmpty() }.map { it.key }.toSet()

// TODO: remove all !!

/**
 * Get the empty nonterminals in a parse rules (i.e., it matches no strings).
 *
 * A nonterminal will be empty if it has no productions or all its productions
 * reference other empty nonterminals.  Essentially this is a recursive version
 * of [productionlessNonterminals].
 *
 * @receiver the parse rules from which to get the empty nonterminals
 * @return the nonterminals that are empty in the parse rules
 */
fun ParseRules.emptyNonterminals(): Set<Nonterminal> {
  val uses: Map<Symbol, Set<Nonterminal>> = this.nonterminalsUsing()
  val empty: QueueSet<Nonterminal> = this.productionlessNonterminals().toCollection(QueueSet())

  for (nonterminal in empty) {
    for (usingLhs in uses.getOrDefault(nonterminal, emptySet())) {
      val lhsIsEmpty = this.productionMap[usingLhs]!!.all { production ->
        production.parts.any { it.second in empty }
      }
      if (lhsIsEmpty) empty += usingLhs
    }
  }

  return empty.toSet()
}

/**
 * Find the symbols that are defined but not used in a [Grammar].
 *
 * @receiver the grammar from which to compute the unused symbols
 * @return the symbols that are not used anywhere in a grammar
 */
fun Grammar.unusedSymbols(): Set<Symbol> = this.definedSymbols() - this.parseRules.usedSymbols()

/**
 * Find the symbols that are reachable from the [start] symbol of a [Grammar]. TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.reachableSymbols(): Set<Symbol> {
  val reachable: QueueSet<Symbol> = QueueSet()

  reachable += this.start
  for (symbol in reachable) {
    if (symbol is Nonterminal) {
      for (production in this.productionMap.getOrDefault(symbol, emptySet())) {
        for (part in production.parts) {
          reachable += part.second
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
fun ParseRules.initialUses(): Map<Symbol?, ProductionMap> =
  this.productionMap.fromSetsMap()
    .groupBy { it.second.parts.firstOrNull()?.second }
    .mapValues { it.value.toSetsMap() }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.epsilons(): ProductionMap =
  this.productionMap
    .mapValues { entry -> entry.value.filter { it.parts.isEmpty() }.toSet() }
    .filterValues { it.isNotEmpty() }

/**
 * TODO.
 *
 * TODO: Note that there are much more efficient algorithms for this.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.nullable(): ProductionMap {
  val uses: Map<Symbol, ProductionMap> = this.productionsUsing()
  // TODO: Note that productions serves as both a record (Set) and worklist (Queue)
  var productions: QueueSet<Pair<Nonterminal, Rhs>> = QueueSet()
  this.epsilons().map { (lhs, productionSet) -> productionSet.map { productions.add(lhs to it) } }
  var nonterminals: Set<Nonterminal> = productions.map { it.first }.toSet()

  // For each item in the queue until it is empty
  for (workitem in productions) {
    // TODO: For each use of workitems's nonterminal
    for ((lhs, rhsMap) in uses.getOrDefault(workitem.first, emptyMap())) {
      for (rhs in rhsMap) {
        // If rhs is only nullable nonterminals, then the production is nullable
        if (rhs.parts.all { it.second is Nonterminal && it.second in nonterminals }) {
          productions += lhs to rhs // Enqueue the nullable production
          nonterminals += lhs // Record the nullable nonterminal
        }
      }
    }
  }

  return productions.toSetsMap()
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun ParseRules.partiallyNullable(): Map<Nonterminal, Map<Rhs, Set<Int>>> {
  val nullable: Set<Symbol?> = this.nullable().keys

  val result: QueueMap<Nonterminal, QueueMap<Rhs, QueueSet<Int>>> =
    queueMap { queueMap { QueueSet() } }

  for ((lhs, rhs) in this.productionMap.fromSetsMap()) {
    var consumed = 0
    do {
      result[lhs][rhs].add(consumed)
    } while (rhs.parts.getOrNull(consumed++)?.second in nullable)
  }

  return result
}
