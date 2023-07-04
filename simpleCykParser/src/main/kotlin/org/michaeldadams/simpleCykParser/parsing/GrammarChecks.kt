/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.grammar.*

/**
 * Find productions that use undefined symbols.
 *
 * In a well-formed grammar, this function will return the empty set.
 *
 * @return pairs of the productions using undefined symbols and the position
 * of the undefined symbol in the [rhs] of that production
 */
fun Grammar.undefinedSymbols(): Set<Pair<Production, Int>> =
  parseRules.productions.values.flatten().flatMap { prod ->
    prod.rhs.mapIndexedNotNull { i, s -> if (s in symbols) null else Pair(prod, i) }
  }.toSet()

fun Grammar.unusedSymbols(): Set<Symbol> = TODO()
fun Grammar.recursivelyUnusedSymbols(): Set<Symbol> = TODO()

fun Grammar.emptyNonterminals(): Set<Nonterminal> = TODO()
fun Grammar.recursivelyEmptyNonterminals(): Set<Nonterminal> = TODO()
