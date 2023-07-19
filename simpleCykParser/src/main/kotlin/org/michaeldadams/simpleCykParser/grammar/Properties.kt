/**
 * Properties that can be computed about lex rules, parse rules and grammars.
 *
 * Note that not everything here is actually used for parsing.  For example,
 * some things like [undefinedSymbols] are used for checking if a grammar is
 * valid.
 */

package org.michaeldadams.simpleCykParser.grammar

import org.michaeldadams.simpleCykParser.util.QueueSet
import org.michaeldadams.simpleCykParser.util.fromSetsMap
import org.michaeldadams.simpleCykParser.util.toSetsMap

// TODO: check @throws

// TODO: check for unique terminal and nonterminal names

/**
 * Get the productions using each symbol.
 *
 * @receiver the parse rules to get what productions use each symbol for
 * @return a map from symbols to a map from nonterminals to the set of
 *   right-hand sides in the nonterminal using the symbol
 */
fun ParseRules.productionsUsing(): Map<Symbol, ProductionMap> =
  this.productionMap.fromSetsMap()
    .flatMap { (lhs, rhs) -> rhs.elements.map { it.symbol to (lhs to rhs) } }
    .toSetsMap()
    .mapValues { it.value.toSetsMap() }

/**
 * Get the nonterminals using each symbol.
 *
 * @receiver the parse rules to get what nonterminals use each symbol for
 * @return a map from symbols to the set of nonterminals that use those symbols
 */
fun ParseRules.nonterminalsUsing(): Map<Symbol, Set<Nonterminal>> =
  this.productionsUsing().mapValues { it.value.keys }

/**
 * Get all the symbols used in the right-hand sides ([Rhs]) of a [ParseRules].
 *
 * @receiver the parse rules to get the used symbols for
 * @return all the symbols used in the right-hand sides of the given parse rules
 */
fun ParseRules.usedSymbols(): Set<Symbol> =
  this.productionMap.values.flatten().flatMap { rhs -> rhs.elements.map { it.symbol } }.toSet() +
    this.start

/**
 * Get the terminals defined by a [LexRules].
 *
 * @receiver the lexing rules to get the defined terminals for
 * @return all the terminals defined in the given lexing rules
 */
fun LexRules.definedTerminals(): Set<Terminal> = this.terminalRules.map { it.terminal }.toSet()

/**
 * Get the nonterminals defined by a [ParseRules].
 *
 * @receiver the parse rules to get the defined nonterminals for
 * @return all the nonterminals defined in the given parse rules
 */
fun ParseRules.definedNonterminals(): Set<Nonterminal> = this.productionMap.keys

/**
 * Get the symbols (terminals and nonterminals) defined by a [Grammar].
 *
 * @receiver the grammar to get the defined symbols for
 * @return all the symbols defined in the given grammar
 */
fun Grammar.definedSymbols(): Set<Symbol> =
  this.lexRules.definedTerminals() + this.parseRules.definedNonterminals()

/**
 * Find productions that use undefined symbols.
 *
 * In a well-formed grammar, this function will return the empty set.
 *
 * @receiver the grammar to get the undefined symbols for
 * @return triples of the left-hand size ([Nonterminal]), right-hand side
 *   ([Rhs]) and the position ([Int]) of the undefined symbol in the right-hand
 *   side
 */
fun Grammar.undefinedSymbols(): Set<Triple<Nonterminal, Rhs, Int>> {
  val symbols = this.definedSymbols()
  return this.parseRules.productionMap.fromSetsMap().flatMap { (lhs, rhs) ->
    rhs.elements.mapIndexedNotNull { i, element ->
      if (element.symbol in symbols) null else Triple(lhs, rhs, i)
    }
  }.toSet()
}

/**
 * Get the nonterminals that have no production.
 *
 * @receiver the parse rules to get the productionless nonterminals for
 * @return the nonterminals that have no production in the parse rules
 */
fun ParseRules.productionlessNonterminals(): Set<Nonterminal> =
  this.productionMap.entries.filter { it.value.isEmpty() }.map { it.key }.toSet()

// TODO: remove all !!

/**
 * Get the empty nonterminals in a parse rules (i.e., they match no strings).
 *
 * A nonterminal will be empty if it has no productions or all its productions
 * reference other empty nonterminals.  Essentially this is a recursive version
 * of [productionlessNonterminals].
 *
 * @receiver the parse rules to get the empty nonterminals for
 * @return the nonterminals that are empty in the parse rules
 */
fun ParseRules.emptyNonterminals(): Set<Nonterminal> {
  val uses: Map<Symbol, Set<Nonterminal>> = this.nonterminalsUsing()
  val empty: QueueSet<Nonterminal> = this.productionlessNonterminals().toCollection(QueueSet())

  for (nonterminal in empty) {
    for (usingLhs in uses.getOrDefault(nonterminal, emptySet())) {
      val lhsIsEmpty = this.productionMap[usingLhs]!!.all { rhs ->
        rhs.elements.any { it.symbol in empty } // TODO: simplify
      }
      if (lhsIsEmpty) empty += usingLhs
    }
  }

  return empty.toSet()
}

/**
 * Find the symbols that are defined but not used in a [Grammar].
 *
 * @receiver the grammar to compute the unused symbols for
 * @return the symbols that are not used anywhere in a grammar
 */
fun Grammar.unusedSymbols(): Set<Symbol> = this.definedSymbols() - this.parseRules.usedSymbols()

/**
 * Find the symbols that are reachable from the [start] symbol of a
 * [ParseRules].
 *
 * A symbol is reachable if it is the [start] symbol or it is referenced in a
 * right-hand side of a reachable symbol.
 *
 * @receiver the parse rules to compute the reachable symbols for
 * @return the symbols that are reachable from the start symbol of the parse
 *   rules
 */
fun ParseRules.reachableSymbols(): Set<Symbol> {
  val reachable: QueueSet<Symbol> = QueueSet()

  reachable += this.start
  for (symbol in reachable) {
    if (symbol is Nonterminal) {
      for (rhs in this.productionMap.getOrDefault(symbol, emptySet())) {
        for (element in rhs.elements) {
          reachable += element.symbol
        }
      }
    }
  }

  return reachable.toSet()
}

/**
 * Find the symbols defined in a grammar that are not reachable from the [start]
 * symbol of the [parseRules] of a [Grammar].
 *
 * Conceptually this is the negation of [reachableSymbols] or a trasitively
 * closed version of [unusedSymbols].
 *
 * @receiver the grammar to compute the unreachable symbols for
 * @return the symbols that are defined in the grammar but are not reachable
 *   from the start symbol of the parse rules in the grammar
 */
fun Grammar.unreachableSymbols(): Set<Symbol> =
  this.definedSymbols() - this.parseRules.reachableSymbols()

/**
 * Find the right-hand sides that use a symbol as their first TODO.
 *
 * TODO: explain null = rhs with no parts
 *
 * @receiver TODO
 * @return a map from a symbol to a map from a nonterminal to a set of the
 *   right-hand sides in that nonterminal that use that symbol as the first
 *   element in their part
 */
fun ParseRules.initialUses(): Map<Symbol?, ProductionMap> =
  this.productionMap.fromSetsMap()
    .groupBy { it.second.elements.firstOrNull()?.symbol }
    .mapValues { it.value.toSetsMap() }

/**
 * Find all right-hand sides with an empty list for their parts (i.e., epsilon
 * productions).
 *
 * @receiver the parse rules in which to find epsilon productions
 * @return a map from nonterminals to the set of right-hand sides for that
 *   nonterminal with an empty list for their parts
 */
fun ParseRules.epsilons(): ProductionMap =
  this.productionMap
    .mapValues { entry -> entry.value.filter { it.elements.isEmpty() }.toSet() }
    .filterValues { it.isNotEmpty() } // TODO: do others need this?

/**
 * Find right-hand sides that are nullable (i.e., they match the empty string).
 *
 * A right-hand side will be nullable if its parts are the empty list or
 * all of the symbols in its parts are nullable.
 *
 * A symbol if nullable if it is a nonterminal and at least one of its
 * right-hand sides is nullable.
 *
 * Note that there are more efficient algorithms for this, but this one is
 * simple to implement and easy to get correct.
 *
 * @receiver the parse rules to find nullable productions for
 * @return a map from nonterminals to the set of right-hand sides for that
 *   nonterminal that are nullable
 */
fun ParseRules.nullable(): ProductionMap {
  val uses: Map<Symbol, ProductionMap> = this.productionsUsing()

  // NOTE: productions serves as both a record (Set) and worklist (Queue)
  var productions: QueueSet<Pair<Nonterminal, Rhs>> = QueueSet()
  this.epsilons().map { (lhs, productionSet) -> productionSet.map { productions.add(lhs to it) } }

  var nullable: Set<Nonterminal> = productions.map { it.first }.toSet()

  // For each item in the queue until it is empty
  for (workitem in productions) {
    // TODO: For each use of workitems's nonterminal
    for ((lhs, rhsMap) in uses.getOrDefault(workitem.first, emptyMap())) {
      for (rhs in rhsMap) {
        // If rhs is only nullable nonterminals, then the production is nullable
        if (rhs.elements.all { it.symbol in nullable }) {
          productions += lhs to rhs // Enqueue the nullable production
          nullable += lhs // Record the nullable nonterminal
        }
      }
    }
  }

  return productions.toSetsMap()
}

/**
 * Find the prefixes of right-hand sides that are nullable (i.e. the prefix
 * matches the empty string).
 *
 * @receiver the parse rules to find nullable prefixes for
 * @return a map from nonterminals to a map from right-hand sides of that
 *   nonterminal to the number of symbols at the start of that right-hand side
 *   that are nullable
 */
fun ParseRules.nullablePrefixes(): Map<Nonterminal, Map<Rhs, Int>> {
  val nullable: Set<Symbol?> = this.nullable().keys

  val result: MutableMap<Nonterminal, MutableMap<Rhs, Int>> = mutableMapOf()

  for ((lhs, rhs) in this.productionMap.fromSetsMap()) {
    val index = rhs.elements.indexOfFirst { it.symbol !in nullable }
    result.getOrPut(lhs, { mutableMapOf() }) += rhs to (if (index == -1) rhs.elements.size else index)
  }

  return result
}
