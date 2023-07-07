/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.grammar

// TODO: check @throws

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
fun Grammar.unusedSymbols(): Set<Symbol> = TODO()

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.recursivelyUnusedSymbols(): Set<Symbol> = TODO()

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.emptyNonterminals(): Set<Nonterminal> = TODO()

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Grammar.recursivelyEmptyNonterminals(): Set<Nonterminal> = TODO()
