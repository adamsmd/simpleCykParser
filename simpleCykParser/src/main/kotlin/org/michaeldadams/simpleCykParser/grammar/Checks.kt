/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.grammar

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
 * Find productions that use undefined symbols.
 *
 * In a well-formed grammar, this function will return the empty set.
 *
 * @receiver TODO
 * @return pairs of the productions using undefined symbols and the position
 * of the undefined symbol in the [rhs] of that production
 */
fun Grammar.undefinedSymbols(): Set<Pair<Production, Int>> = TODO()

// parseRules.productions.values.flatten().flatMap { prod ->
//   prod.rhs.mapIndexedNotNull { i, s -> if (s in symbols) null else Pair(prod, i) }
// }.toSet()

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.productionlessNonterminals(): Set<Nonterminal> = TODO()

/**
 * TODO.
 *
 * Recursive version of productionless
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.emptyNonterminals(): Set<Nonterminal> = TODO()
// {
//   val uses: Map<Symbol, Set<Production>> = this.productionsUsing()
//   val empty = this.productionlessNonTerminals().toCollection { QueueSet() }

//   for (nonterminal in empty) {
//     for (usingProduction in uses.getOrDefault(workitem, emptySet())) {
//       val lhsIsEmpty = this.parseRules.productionMap[usingProduction.lhs].all { production ->
//         production.rhs.any { it.second in empty }
//       }
//       if (lhsIsEmpty) empty += usingProduction.lhs
//     }
//   }
// }

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.unusedSymbols(): Set<Symbol> = TODO()

/**
 * TODO.
 *
 * Recursive version of unused
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.unreachableSymbols(emptyNonTerminals: Set<Nonterminal>): Set<Symbol> = TODO()
