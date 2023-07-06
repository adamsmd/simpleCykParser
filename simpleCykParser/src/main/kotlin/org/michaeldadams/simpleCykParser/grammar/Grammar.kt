/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.grammar

import kotlin.text.Regex

/**************************************/
/* Terminals and Nonterminals        */
/**************************************/

/**
 * A terminal or nonterminal in the grammar.  Note that terminals and
 * nonterminals have separate namespaces.
 */
sealed interface Symbol

/**
 * A terminal in the grammar.
 *
 * @property name the name of the terminal
 */
data class Terminal(val name: String) : Symbol

/**
 * A nonterminal in the grammar.
 *
 * @property name the name of the nonterminal
 */
data class Nonterminal(val name: String) : Symbol

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
// TODO: rename lexRules to terminals
data class LexRules(val whitespace: Regex, val lexRules: List<LexRule>) {
  /** The terminals defined in these lexical rules. */
  val terminals: Set<Terminal> by lazy { lexRules.map { it.terminal }.toSet() }
}
// TODO: replace whitespace with a post processing filter
// TODO: implement indent as a post processing filter

/**************************************/
/* Parsing                            */
/**************************************/

/**
 * A production rule for a nonterminal.
 *
 * @property lhs the nonterminal that this production is for
 * @property name an optional name for this production
 * @property rhs the symbols that this production expands to
 */
data class Production(val lhs: Nonterminal, val name: String?, val rhs: List<Symbol>)
// TODO: allow names of rhs fields

/**
 * The combined parsing rules of a language.
 *
 * @property start the start symbol of the language
 * @property productions a map from a nonterminal to the set of productions for that nonterminal
 */
// TODO: rename productions to productionMap
data class ParseRules(val start: Symbol, val productions: Map<Nonterminal, Set<Production>>) {
  /** The nonterminals defined in these parse rules. */
  val nonterminals: Set<Nonterminal> by lazy { productions.keys }
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
  /** The terminals defined in this grammar. */
  val terminals get() = lexRules.terminals

  /** The nonterminals defined in this grammar. */
  val nonterminals get() = parseRules.nonterminals

  /** The symbols defined in this grammar. */
  val symbols: Set<Symbol> by lazy { terminals + nonterminals }
}
