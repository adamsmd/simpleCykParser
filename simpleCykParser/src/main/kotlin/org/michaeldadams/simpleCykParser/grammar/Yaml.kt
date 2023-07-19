/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.grammar

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.MissingRequiredPropertyException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlList
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import kotlinx.serialization.builtins.serializer
import org.michaeldadams.simpleCykParser.util.toEqRegex
import kotlin.text.toRegex

// TODO: check that all KDoc have @receiver

/**
 * Parses a string as Yaml containing a map.
 *
 * @receiver TODO
 * @return the map resulting from parsing the string
 */
fun String.toYamlMap(): YamlMap = Yaml.default.parseToYamlNode(this).yamlMap

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
    val (terminal, regex) = item.yamlMap.toMapPair()
    TerminalRule(Terminal(terminal), regex.yamlScalar.content.toRegex().toEqRegex())
  }

  // TODO: regex RegexOption.COMMENTS

  return LexRules(whitespace, terminals)
}

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

  val productionMap = productionsYaml.entries.map { entry ->
    Nonterminal(entry.key.content) to
      entry.value.yamlList.items.map { it.toRhs(nonterminals) }.toSet()
  }.toMap()

  return ParseRules(Nonterminal(start), productionMap)
}
private val WHITESPACE_REGEX = "\\p{IsWhite_Space}+".toRegex()
private const val NONTERMINAL_PREFIX = "N:"
private const val TERMINAL_PREFIX = "T:"

fun String.toSymbol(nonterminals: Set<String>): Symbol =
  when {
    this.startsWith(NONTERMINAL_PREFIX) -> Nonterminal(this.substring(NONTERMINAL_PREFIX.length))
    this.startsWith(TERMINAL_PREFIX) -> Terminal(this.substring(TERMINAL_PREFIX.length))
    this in nonterminals -> Nonterminal(this)
    else -> Terminal(this)
  }

fun YamlNode.toSymbol(nonterminals: Set<String>): Symbol =
  this.yamlScalar.content.toSymbol(nonterminals)

fun YamlNode.toRhsElement(nonterminals: Set<String>): RhsElement {
  val (label, symbol) = this.toOptionalMapPair()
  return RhsElement(label, symbol.toSymbol(nonterminals))
}
  // when (this) {
  //   is YamlMap -> this.toMapPair().let { RhsElement(it.first, it.second.toSymbol(nonterminals)) }
  //   else -> RhsElement(null, this.toSymbol(nonterminals))
  // }
  // this.toOptionalListPair().let { RhsElement(it.first, it.second.toSymbol(nonterminals)) }

fun YamlNode.toRhsElements(nonterminals: Set<String>): List<RhsElement> =
  when (this) {
    is YamlList -> this.items.map { it.toRhsElement(nonterminals) }
    is YamlScalar -> this.content.split(WHITESPACE_REGEX)
      .filter { it.isNotEmpty() }
      .map { RhsElement(null, it.toSymbol(nonterminals)) }
    else -> throw incorrectType("YamlScalar or YamlList", this)
  }

fun YamlNode.toRhs(nonterminals: Set<String>): Rhs {
  val (label, elements) = this.toOptionalMapPair()
  return Rhs(label, elements.toRhsElements(nonterminals))
}

private fun incorrectType(expectedType: String, yamlNode: YamlNode): IncorrectTypeException =
  IncorrectTypeException(
    "Expected element to be ${expectedType} but is ${yamlNode::class.simpleName}",
    yamlNode.path,
  )

/*

TODO:

whitespace: \s+
terminals:
  - STRING: '"[^"]"'
  - NUM: \d+
  - IF: if
  - (: \(
  - STR: (?idmsuxU-idmsuxU) ... TODO (note some flags have no inline)
terminalOptions: COMMENTS, UNICODE
start: S
productions:
  S:
    - F: if ( S ) then { else }
    - F: '" S "'
    - S S
    - ""
    - []
    - [X: S, Y: S]
    - F: [X: S, S]
    - F:
       - X: S
       - S
  T: []

*/

/**
 * Extracts a grammar from a [YamlMap].
 *
 * @receiver TODO
 * @return the grammar extracted from the [YamlMap]
 */
fun YamlMap.toGrammar(): Grammar = Grammar(this.toLexRules(), this.toParseRules())

fun RhsElement.toYamlString(): String {
  val symbol = this.symbol.toYamlString()
  return if (this.label == null) symbol else "[${label.toYamlString()}, ${symbol}]"
}

/**
 * TODO.
 *
 * @receiver TODO
 * @return TODO
 */
fun Rhs.toYamlString(): String {
  val elements = "[${this.elements.map { "${it.toYamlString()}" }.joinToString() }]"
  return if (this.label == null) elements else "${this.label}: ${elements}"
}

// fun Pair<String?, Symbol>.toYamlString(): String =
//   if (this.first === null) this.second.toYamlString() else "${this.first}: ${this.second.toYamlString()}"

fun Symbol.toYamlString(): String =
  when (this) {
    is Terminal -> (TERMINAL_PREFIX + name).toYamlString()
    is Nonterminal -> (NONTERMINAL_PREFIX + name).toYamlString()
  }
// ["S"]
// "S"

// ================================================================== //
// Private Helpers
// ================================================================== //

private operator fun Map<String, YamlNode>.get(key: String, path: YamlPath): YamlNode =
  this[key] ?: throw MissingRequiredPropertyException(key, path)

private fun YamlMap.toMap(): Map<String, YamlNode> = this.entries.mapKeys { it.key.content }

// TODO: rename to toPair
private fun YamlMap.toMapPair(): Pair<String, YamlNode> {
  // TODO: not require
  require(this.entries.size == 1) { "Expected one map element but found ${this.entries.size}" }
  val pair = this.entries.toList().single()
  return pair.first.content to pair.second
}

private fun YamlNode.toOptionalMapPair(): Pair<String?, YamlNode> =
  when (this) {
    is YamlMap -> this.toMapPair()
    else -> null to this
  }

private fun String.toYamlString(): String =
  Yaml.default.encodeToString(String.serializer(), this)
