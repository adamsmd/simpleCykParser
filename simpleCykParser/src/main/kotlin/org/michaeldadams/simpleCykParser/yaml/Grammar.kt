/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.MissingRequiredPropertyException
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlList
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.LexRules
import org.michaeldadams.simpleCykParser.grammar.Nonterminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Rhs
import org.michaeldadams.simpleCykParser.grammar.RhsElement
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.Terminal
import org.michaeldadams.simpleCykParser.grammar.TerminalRule
import org.michaeldadams.simpleCykParser.util.toEqRegex
import kotlin.text.toRegex

// TODO: check that all KDoc have @receiver

// ================================================================== //
// Symbols
// ================================================================== //

private const val TERMINAL_PREFIX = "T:"
private const val NONTERMINAL_PREFIX = "N:"

/**
 * Convert a string into the terminal or nonterminal it represents.
 *
 * If the string starts with "T:", it is a terminal with its name after this prefix.
 * If the string starts with "N:", it is a nonterminal with its name after this prefix.
 * If neither is the case, the string is a nonterminal if it is in [nonterminals].
 * Otherwise, it is a terminal.
 *
 * @receiver the string to be converted to a symbol
 * @param nonterminals the set of strings to treat as nonterminals
 * @return the symbol that the string was converted into
 */
fun String.toSymbol(nonterminals: Set<String>): Symbol =
  when {
    this.startsWith(NONTERMINAL_PREFIX) -> Nonterminal(this.substring(NONTERMINAL_PREFIX.length))
    this.startsWith(TERMINAL_PREFIX) -> Terminal(this.substring(TERMINAL_PREFIX.length))
    this in nonterminals -> Nonterminal(this)
    else -> Terminal(this)
  }

/**
 * Convert a [YamlNode] into the terminal or nonterminal it represents.
 *
 * If the [YamlNode] is a [YamlScalar], [toSymbol] is called on the [content] of
 * that [YamlScalar].  If the [YamlNode is not a [YamlScalar], an exception is
 * thrown.
 *
 * @receiver the [YamlNode] to be converted to a symbol
 * @param nonterminals the set of string to treat as nonterminals
 * @return the symbol that the [YamlNode] was converted into
 */
fun YamlNode.toSymbol(nonterminals: Set<String>): Symbol =
  this.yamlScalar.content.toSymbol(nonterminals)

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Symbol.toYamlString(): String =
  when (this) {
    is Terminal -> (TERMINAL_PREFIX + name).toYamlString()
    is Nonterminal -> (NONTERMINAL_PREFIX + name).toYamlString()
  }

// ================================================================== //
// RhsElement
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @param nonterminals TODO
 * @return TODO
 */
fun YamlNode.toRhsElement(nonterminals: Set<String>): RhsElement {
  val (label, symbol) = this.toOptionalPair()
  return RhsElement(label, symbol.toSymbol(nonterminals))
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun RhsElement.toYamlString(): String {
  val symbol = this.symbol.toYamlString()
  return if (this.label == null) symbol else "[${label.toYamlString()}, ${symbol}]"
}

private val WHITESPACE_REGEX = "\\p{IsWhite_Space}+".toRegex()

/**
 * TODO.
 *
 * @receiver TODO
 * @param nonterminals TODO
 * @return TODO
 */
fun YamlNode.toRhsElements(nonterminals: Set<String>): List<RhsElement> =
  when (this) {
    is YamlList -> this.items.map { it.toRhsElement(nonterminals) }
    is YamlScalar -> this.content.split(WHITESPACE_REGEX)
      .filter { it.isNotEmpty() }
      .map { RhsElement(null, it.toSymbol(nonterminals)) }
    else -> throw incorrectType("YamlScalar or YamlList", this)
  }

// ================================================================== //
// Rhs
// ================================================================== //

/**
 * TODO.
 *
 * @receiver TODO
 * @param nonterminals TODO
 * @return TODO
 */
fun YamlNode.toRhs(nonterminals: Set<String>): Rhs {
  val (label, elements) = this.toOptionalPair()
  return Rhs(label, elements.toRhsElements(nonterminals))
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Rhs.toYamlString(): String {
  val elements = "[${this.elements.map { "${it.toYamlString()}" }.joinToString() }]"
  return if (this.label == null) elements else "${this.label.toYamlString()}: ${elements}"
}

// ================================================================== //
// LexRules
// ================================================================== //

/**
 * Extracts lexing rules from a [YamlMap].
 *
 * @receiver TODO
 * @return the lexing rules extracted from the [YamlMap]
 */
fun YamlMap.toLexRules(): LexRules {
  val map = this.toMap()
  val whitespace = map["whitespace", this.path].yamlScalar.content.toRegex().toEqRegex()

  val terminals = map["terminals", this.path].yamlList.items.map { item ->
    val (terminal, regex) = item.yamlMap.toPair()
    TerminalRule(Terminal(terminal), regex.yamlScalar.content.toRegex().toEqRegex())
  }

  // TODO: regex RegexOption.COMMENTS

  return LexRules(whitespace, terminals)
}

// ================================================================== //
// ParseRules
// ================================================================== //

/**
 * Extracts parsing rules from a [YamlMap].
 *
 * @receiver TODO
 * @return the parsing rules extracted from the [YamlMap]
 * @throws IncorrectTypeException TODO
 * @throws MissingRequiredPropertyException TODO
 */
fun YamlMap.toParseRules(): ParseRules {
  val map = this.toMap()
  val start: String = map["start", this.path].yamlScalar.content
  val productionsYaml: Map<YamlScalar, YamlNode> = map["productions", this.path].yamlMap.entries
  val nonterminals: Set<String> = productionsYaml.keys.map { it.content }.toSet()

  val productionMap = productionsYaml.map { entry ->
    Nonterminal(entry.key.content) to
      entry.value.yamlList.items.map { it.toRhs(nonterminals) }.toSet()
  }.toMap()

  return ParseRules(Nonterminal(start), productionMap)
}

// ================================================================== //
// Grammar
// ================================================================== //

/**
 * Extracts a grammar from a [YamlMap].
 *
 * @receiver TODO
 * @return the grammar extracted from the [YamlMap]
 */
fun YamlMap.toGrammar(): Grammar = Grammar(this.toLexRules(), this.toParseRules())

// ================================================================== //
// Private Helpers
// ================================================================== //

// fun incorrectType(expectedType: String, yamlNode: YamlNode): IncorrectTypeException =
//   IncorrectTypeException(
//     "Expected element to be ${expectedType} but is ${yamlNode::class.simpleName}",
//     yamlNode.path,
//   )

private operator fun Map<String, YamlNode>.get(key: String, path: YamlPath): YamlNode =
  this[key] ?: throw MissingRequiredPropertyException(key, path)

private fun YamlMap.toMap(): Map<String, YamlNode> = this.entries.mapKeys { it.key.content }
