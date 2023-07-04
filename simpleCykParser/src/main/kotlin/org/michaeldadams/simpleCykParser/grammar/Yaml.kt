/** Functions for converting Yaml to a grammar. */

package org.michaeldadams.simpleCykParser.grammar.yaml

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.MissingRequiredPropertyException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import org.michaeldadams.simpleCykParser.grammar.Grammar
import org.michaeldadams.simpleCykParser.grammar.LexRule
import org.michaeldadams.simpleCykParser.grammar.LexRules
import org.michaeldadams.simpleCykParser.grammar.NonTerminal
import org.michaeldadams.simpleCykParser.grammar.ParseRules
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Terminal
import kotlin.text.Regex
import kotlin.text.toRegex

fun mapFromYamlString(string: String): Map<String, YamlNode> =
  Yaml.default.parseToYamlNode(string).cast<YamlMap>().entries.mapKeys { it.key.content }

fun lexRulesFromMap(yamlMap: Map<String, YamlNode>): LexRules {
  val whitespace: Regex = yamlMap.getYaml<YamlScalar>("whitespace").content.toRegex()

  val terminals = yamlMap.getYaml<YamlList>("terminals").items.map {
    it.cast<YamlMap>().asPair()
  }.map { LexRule(Terminal(it.first), it.second.toRegex()) }
  // TODO: regex RegexOption.COMMENTS

  return LexRules(whitespace, terminals)
}

fun parseRulesFromMap(yamlMap: Map<String, YamlNode>): ParseRules {
  val start: String = yamlMap.getYaml<YamlScalar>("start").content
  val productionsYaml: Map<YamlScalar, YamlNode> = yamlMap.getYaml<YamlMap>("productions").entries
  val nonterminals: Set<String> = productionsYaml.keys.map { it.content }.toSet()

  val whitespaceRegex = "\\p{IsWhite_Space}+".toRegex()
  val productionsMap = productionsYaml.entries.map { entry ->
    NonTerminal(entry.key.content) to
      entry.value.cast<YamlList>().items.map {
        val (name, rhsString) = when (it) {
          is YamlMap -> it.asPair()
          is YamlScalar -> null to it.content
          else -> incorrectType(it, "YamlMap or YamlScalar")
        }
        val rhs = rhsString
          .split(whitespaceRegex)
          .filter { it.isNotEmpty() }
          .map { if (it in nonterminals) NonTerminal(it) else Terminal(it) }
        Production(NonTerminal(entry.key.content), name, rhs)
      }.toSet()
  }.toMap()

  return ParseRules(NonTerminal(start), productionsMap)
}

/*

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
  T: []

*/

fun grammarFromMap(yamlMap: Map<String, YamlNode>): Grammar =
  Grammar(lexRulesFromMap(yamlMap), parseRulesFromMap(yamlMap))

/**********************/
/* Helpers */
/*********************/

private fun incorrectType(node: YamlNode, type: String): Nothing =
  throw IncorrectTypeException("Value is not a ${type}.", node.path)

private inline fun <reified T : YamlNode> YamlNode.cast(): T =
  this as? T ?: incorrectType(this, T::class.java.name)

private inline fun <reified T : YamlNode> Map<String, YamlNode>.getYaml(key: String): T =
  (this[key] ?: throw MissingRequiredPropertyException(key, YamlPath.root)).cast<T>()

private fun YamlMap.asPair(): Pair<String, String> {
  if (this.entries.size < 1) TODO()
  if (this.entries.size > 1) TODO()
  val p = this.entries.toList().single()
  return p.first.content to (p.second.cast<YamlScalar>()).content
}
