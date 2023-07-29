/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.yaml

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
 * @receiver the string to convert to a symbol
 * @param nonterminals the strings to be treated as names of nonterminals
 * @return the symbol represented by the string
 */
fun String.toSymbol(nonterminals: Set<String>): Symbol =
  when {
    this.startsWith(NONTERMINAL_PREFIX) -> Nonterminal(this.substring(NONTERMINAL_PREFIX.length))
    this.startsWith(TERMINAL_PREFIX) -> Terminal(this.substring(TERMINAL_PREFIX.length))
    this in nonterminals -> Nonterminal(this)
    else -> Terminal(this)
  }

/**
 * Convert a YAML node to a [Symbol] object.
 *
 * @receiver the YAML node to convert to an object
 * @param nonterminals the strings to be treated as names of nonterminals
 * @return the object represented by the YAML node
 */
fun YamlNode.toSymbol(nonterminals: Set<String>): Symbol =
  this.yamlScalar.content.toSymbol(nonterminals)

/**
 * Convert a [Symbol] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
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
 * Convert a YAML node to an [RhsElement] object.
 *
 * @receiver the YAML node to convert to an object
 * @param nonterminals the strings to be treated as names of nonterminals
 * @return the object represented by the YAML node
 */
fun YamlNode.toRhsElement(nonterminals: Set<String>): RhsElement {
  val (label, symbol) = this.toOptionalPair()
  return RhsElement(label, symbol.toSymbol(nonterminals))
}

/**
 * Convert an [RhsElement] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun RhsElement.toYamlString(): String {
  val symbol = this.symbol.toYamlString()
  return if (this.label == null) symbol else "[${label.toYamlString()}, ${symbol}]"
}

@Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
private val WHITESPACE_REGEX = "\\p{IsWhite_Space}+".toRegex()

/**
 * Convert a YAML node to a list of [RhsElement] objects.
 *
 * @receiver the YAML node to convert to the objects
 * @param nonterminals the strings to be treated as names of nonterminals
 * @return the list of objects represented by the YAML node
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
 * Convert a YAML node to an [Rhs] object.
 *
 * @receiver the YAML node to convert to an object
 * @param nonterminals the strings to be treated as names of nonterminals
 * @return the object represented by the YAML node
 */
fun YamlNode.toRhs(nonterminals: Set<String>): Rhs {
  val (label, elements) = this.toOptionalPair()
  return Rhs(label, elements.toRhsElements(nonterminals))
}

/**
 * Convert an [Rhs] object to its YAML representation.
 *
 * @receiver the object to convert to YAML
 * @return the YAML resulting from converting the object
 */
fun Rhs.toYamlString(): String {
  val elements = "[${this.elements.map { "${it.toYamlString()}" }.joinToString() }]"
  return if (this.label == null) elements else "${this.label.toYamlString()}: ${elements}"
}

// ================================================================== //
// LexRules
// ================================================================== //

/**
 * Convert a YAML node to a [LexRules] object.
 *
 * @receiver the YAML node to convert to an object
 * @return the object represented by the YAML node
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
 * Convert a YAML node to a [ParseRules] object.
 *
 * @receiver the YAML node to convert to an object
 * @return the object represented by the YAML node
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
 * Convert a YAML node to a [Grammar] object.
 *
 * @receiver the YAML node to convert to an object
 * @return the object represented by the YAML node
 */
fun YamlMap.toGrammar(): Grammar = Grammar(this.toLexRules(), this.toParseRules())

// ================================================================== //
// Private Helpers
// ================================================================== //

/**
 * Version of [get] that throws [MissingRequiredPropertyException] if the key is
 * not present.
 *
 * @receiver the [Map] in which to lookup the key
 * @param key the key to lookup in the [Map]
 * @param path the path to pass to [MissingRequiredPropertyException] if the key
 *   is not found
 * @return the value for the given key
 * @throw MissingRequiredPropertyException if the key is not present
 */
private operator fun Map<String, YamlNode>.get(key: String, path: YamlPath): YamlNode =
  this[key] ?: throw MissingRequiredPropertyException(key, path)

/**
 * Convert a [YamlMap] to a [Map] keyed by strings.
 *
 * Use this instead of the [entries] of [YamlMap] because that is keyed by
 * [YamlNode].
 *
 * @receiver the [YamlMap] to convert
 * @return the [Map] resulting from the conversion
 */
private fun YamlMap.toMap(): Map<String, YamlNode> = this.entries.mapKeys { it.key.content }
