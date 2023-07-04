/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.grammar

import kotlin.text.Regex

/**************************************/
/* Terminals and Non-Terminals        */
/**************************************/

/**
 * A terminal or non-terminal in the grammar.  Note that terminals and
 * non-terminals have separate namespaces.
 */
sealed interface Symbol

/**
 * A terminal in the grammar.
 *
 * @property name the name of the terminal
 */
data class Terminal(val name: String) : Symbol

/**
 * A non-terminal in the grammar.
 *
 * @property name the name of the non-terminal
 */
data class NonTerminal(val name: String) : Symbol

/**************************************/
/* Lexing                             */
/**************************************/

/**
 * A lexical rule for a terminal.
 *
 * @property terminal the terminal that this lexical rule is for
 * @property regex the regular expression defining the syntax of the terminal
 */
data class LexRule(val terminal: Terminal, val regex: Regex)

/**
 * The combined lexical rules of a language.
 *
 * Note that multiple lexical rules match, the longest match wins.  If there are
 * multiple longest matches, which ever rule is earliest in [lexRules] wins.
 *
 * Also note that multiple rules for the same terminal are allowed.
 *
 * @property whitespace the regular expression defining the synatx of whitespace and comments
 * @property lexRules all of the non-whitespace lexical rules for the language
 */
data class LexRules(val whitespace: Regex, val lexRules: List<LexRule>) {
  // TODO: lazy
  val terminals: Set<Terminal> = lexRules.map { it.terminal }.toSet()
}

/**************************************/
/* Parsing                            */
/**************************************/

/**
 * A production rule for a non-terminal.
 *
 * @property lhs the non-terminal that this production is for
 * @property name an optional name for this production
 * @property rhs the symbols that this production expands to
 */
data class Production(val lhs: NonTerminal, val name: String?, val rhs: List<Symbol>)

/**
 * The combined parsing rules of a language.
 *
 * @property start the start symbol of the language
 * @property productions a map from a non-terminal to the set of productions for that non-terminal
 */
data class ParseRules(val start: Symbol, val productions: Map<NonTerminal, Set<Production>>) {
  val nonterminals: Set<NonTerminal> = productions.keys
}

/**************************************/
/* Lexing and Parsing Together        */
/**************************************/

/**
 * The lexing and parsing rules of a language.
 *
 * @property lexRules the lexing rules of the language
 * @property parseRules the parsing rules of the language
 */
data class Grammar(val lexRules: LexRules, val parseRules: ParseRules) {
  val terminals get() = lexRules.terminals
  val nonterminals get() = parseRules.nonterminals
  val symbols: Set<Symbol> = terminals + nonterminals

  /**
   * Find productions that use undefined symbols.
   *
   * In a well-formed grammar, this function will return the empty set.
   *
   * @return pairs of the productions using undefined symbols and the position
   * of the undefined symbol in the [rhs] of that production
   */
  fun undefinedSymbols(): Set<Pair<Production, Int>> =
    parseRules.productions.values.flatten().flatMap { prod ->
      prod.rhs.mapIndexedNotNull { i, s -> if (s in symbols) null else Pair(prod, i) }
    }.toSet()

  fun unusedSymbols(): Set<Symbol> = TODO()
  fun recursivelyUnusedSymbols(): Set<Symbol> = TODO()

  fun emptyNonTerminals(): Set<NonTerminal> = TODO()
  fun recursivelyEmptyNonTerminals(): Set<NonTerminal> = TODO()
}
