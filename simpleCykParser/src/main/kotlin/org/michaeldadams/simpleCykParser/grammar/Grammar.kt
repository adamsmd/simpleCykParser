/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.grammar

import org.michaeldadams.simpleCykParser.util.EqRegex

// ================================== //
// Terminals and Nonterminals
// ================================== //

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

// ================================== //
// Lexing
// ================================== //

/**
 * A lexical rule for a terminal.
 *
 * @property terminal the terminal that this lexical rule is for
 * @property regex the regular expression defining the syntax of the terminal
 */
data class TerminalRule(val terminal: Terminal, val regex: EqRegex)

/**
 * The combined lexical rules of a language.
 *
 * Note that multiple lexical rules match, the longest match wins.  If there are
 * multiple longest matches, which ever rule is earliest in [terminals] wins.
 *
 * Also note that multiple rules for the same terminal are allowed.
 *
 * @property whitespace the regular expression defining the synatx of whitespace and comments
 * @property terminalRules the list of all the terminals and their all of the non-whitespace lexical rules for the language
 */
data class LexRules(val whitespace: EqRegex, val terminalRules: List<TerminalRule>)

// TODO: replace whitespace with a post processing filter
// TODO: implement indent as a post processing filter

// ================================== //
// Parsing
// ================================== //

/**
 * A production rule for a nonterminal.
 *
 * @property lhs the nonterminal that this production is for
 * @property name an optional name for this production
 * @property rhs optionally named symbols that this production expands to
 */
data class Production(val lhs: Nonterminal, val name: String?, val rhs: List<Pair<String?, Symbol>>) {
  fun toYamlString(): String =
    "${name}: [${rhs.map { "${it.first}: ${it.second}" }.joinToString() }]"
}

/**
 * The combined parsing rules of a language.
 *
 * @property start the start symbol of the language
 * @property productionMap a map from a nonterminal to the set of productions for that nonterminal
 */
data class ParseRules(val start: Symbol, val productionMap: Map<Nonterminal, Set<Production>>)

// ================================== //
// Lexing and Parsing Together
// ================================== //

/**
 * The lexing and parsing rules of a language.
 *
 * @property lexRules the lexing rules of the language
 * @property parseRules the parsing rules of the language
 */
data class Grammar(val lexRules: LexRules, val parseRules: ParseRules)
