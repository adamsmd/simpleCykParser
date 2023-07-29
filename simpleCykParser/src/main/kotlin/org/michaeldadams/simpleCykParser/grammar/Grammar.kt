/** Types for representing a grammar. */

package org.michaeldadams.simpleCykParser.grammar

import org.michaeldadams.simpleCykParser.util.EqRegex
import org.michaeldadams.simpleCykParser.util.Generated as Gen

// ================================================================== //
// Terminals and Nonterminals
// ================================================================== //

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
@Gen data class Terminal(val name: String) : Symbol

/**
 * A nonterminal in the grammar.
 *
 * @property name the name of the nonterminal
 */
@Gen data class Nonterminal(val name: String) : Symbol

// ================================================================== //
// Lexing
// ================================================================== //

/**
 * A lexical rule for a terminal.
 *
 * @property terminal the terminal that this lexical rule is for
 * @property regex the regular expression defining the syntax of the terminal
 */
@Gen data class TerminalRule(val terminal: Terminal, val regex: EqRegex)

/**
 * The combined lexical rules of a language.
 *
 * Multiple rules for the same terminal are allowed.
 *
 * When multiple lexical rules match, the longest match wins.  If there are
 * multiple longest matches, which ever rule is earliest in [terminals] wins.
 *
 * @property whitespace the regular expression defining the synatx of whitespace
 *   and comments
 * @property terminalRules the terminal rules for language
 */
@Gen data class LexRules(val whitespace: EqRegex, val terminalRules: List<TerminalRule>)

// ================================================================== //
// Parsing
// ================================================================== //

/**
 * Labeled symbols in the right-hand side of a production.
 *
 * Note that [label] is not used by the parsing algorithm but may be useful to
 * help users understand the role of different parts of a production.
 *
 * @property label an optional label for this element
 * @property symbol optionally named symbols that this production expands to
 */
@Gen data class RhsElement(val label: String?, val symbol: Symbol)

/**
 * The right-hand side of a (labeled) production.
 *
 * Note that [label] is not used by the parsing algorithm but is useful for
 * debugging and telling one production from another.
 *
 * @property label an optional label for this production
 * @property elements optionally named symbols that this production expands to
 */
@Gen data class Rhs(val label: String?, val elements: List<RhsElement>)

/**
 * The combined parsing rules of a language.
 *
 * Note that this does not represent single productions explicitly.  Instead
 * the [productionMap] maps the left-hand sides of productions to right-hand
 * sides of productions.
 *
 * @property start the start symbol of the language
 * @property productionMap a map from a production left-hand side to the set of
 *   right-hand sides in productions with that left-hand side
 */
@Gen data class ParseRules(val start: Symbol, val productionMap: Map<Nonterminal, Set<Rhs>>)

// ================================================================== //
// Lexing and Parsing Together
// ================================================================== //

/**
 * The lexing and parsing rules of a language.
 *
 * @property lexRules the lexing rules of the language
 * @property parseRules the parsing rules of the language
 */
@Gen data class Grammar(val lexRules: LexRules, val parseRules: ParseRules)
