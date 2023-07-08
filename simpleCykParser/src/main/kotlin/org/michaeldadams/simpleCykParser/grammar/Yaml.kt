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
    item.yamlMap.toPair { it.yamlScalar.content }
  }.map { TerminalRule(Terminal(it.first), it.second.toRegex().toEqRegex()) }

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

  val productionsMap = productionsYaml.entries.map { entry ->
    val nt = Nonterminal(entry.key.content)
    nt to entry.value.yamlList.items.map { parseProduction(nonterminals, nt, it) }.toSet()
  }.toMap()

  return ParseRules(Nonterminal(start), productionsMap)
}
private val whitespaceRegex = "\\p{IsWhite_Space}+".toRegex()

private fun parseRhs(rhs: YamlNode): List<Pair<String?, String>> =
  when (rhs) {
    is YamlScalar ->
      rhs.content.split(whitespaceRegex).filter { it.isNotEmpty() }.map { null to it }
    // TODO: rename item?
    is YamlList -> rhs.items.map { rhsItem ->
      when (rhsItem) {
        is YamlScalar -> null to rhsItem.content
        is YamlMap -> rhsItem.toPair { it.yamlScalar.content }
        else -> throw incorrectType("YamlScalar or YamlMap", rhsItem)
      }
    }
    else -> throw incorrectType("YamlScalar or YamlList", rhs)
  }

private fun parseProduction(nonterminals: Set<String>, nonterminal: Nonterminal, yamlNode: YamlNode): Production {
  val (name, rawRhs) =
    if (yamlNode is YamlMap) yamlNode.toPair(::parseRhs) else null to parseRhs(yamlNode)
  val rhs = rawRhs.map {
    it.first to if (it.second in nonterminals) Nonterminal(it.second) else Terminal(it.second)
  }
  return Production(nonterminal, name, rhs)
}

private fun incorrectType(expectedType: String, yamlNode: YamlNode): IncorrectTypeException =
  IncorrectTypeException(
    "Expected element to be ${expectedType} but is ${yamlNode::class.simpleName}",
    yamlNode.path
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

// ================================== //
// Private Helpers
// ================================== //

private operator fun Map<String, YamlNode>.get(key: String, path: YamlPath): YamlNode =
  this[key] ?: throw MissingRequiredPropertyException(key, path)

private fun YamlMap.toMap(): Map<String, YamlNode> = this.entries.mapKeys { it.key.content }

private fun <T> YamlMap.toPair(f: (YamlNode) -> T): Pair<String, T> {
  require(this.entries.size == 1) { "TODO" }
  val p = this.entries.toList().single()
  return p.first.content to f(p.second)
}
