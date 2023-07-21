/**
 * Properties that can be computed about lex rules, parse rules and grammars.
 *
 * Note that not everything here is actually used for parsing.  For example,
 * some things like [undefinedSymbols] are used for checking if a grammar is
 * valid.
 */

package org.michaeldadams.simpleCykParser.grammar

// TODO: check @throws

// TODO: check for unique terminal and nonterminal names

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
 * @param symbols the symbols to assume as defined
 * @return triples of the left-hand size ([Nonterminal]), right-hand side
 *   ([Rhs]) and the position ([Int]) of the undefined symbol in the right-hand
 *   side
 */
fun ParseRules.undefinedSymbols(symbols: Set<Symbol>): Set<Triple<Nonterminal, Rhs, Int>> =
  this.productionMap.flatMap { (lhs, rhsSet) ->
    rhsSet.flatMap { rhs ->
      rhs.elements.mapIndexedNotNull { i, element ->
        if (element.symbol in symbols) null else Triple(lhs, rhs, i)
      }
    }
  }.toSet()

// TODO: check ParseRules.start is undefined

/**
 * Get the nonterminals that have no production.
 *
 * @receiver the parse rules to get the productionless nonterminals for
 * @return the nonterminals that have no production in the parse rules
 */
fun ParseRules.productionlessNonterminals(): Set<Nonterminal> =
  this.productionMap.filter { it.value.isEmpty() }.map { it.key }.toSet()

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
 * Find the symbols that are defined but not used in a [Grammar].
 *
 * @receiver the grammar to compute the unused symbols for
 * @return the symbols that are not used anywhere in a grammar
 */
fun Grammar.unusedSymbols(): Set<Symbol> = this.definedSymbols() - this.parseRules.usedSymbols()
