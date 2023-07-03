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
  when (val yaml = Yaml.default.parseToYamlNode(string)) {
    is YamlMap -> yaml.entries.mapKeys { it.key.content }
    else -> TODO()
  }

fun lexRulesFromMap(yamlMap: Map<String, YamlNode>): LexRules {
  val whitespace: Regex = yamlMap.getYaml<YamlScalar>("whitespace").content.toRegex()

  val rules = yamlMap.getYaml<YamlList>("terminals").items.map {
    (it as? YamlMap ?: TODO()).asPair()
  }.map { LexRule(Terminal(it.first), it.second.toRegex()) }

  return LexRules(whitespace, rules)
}

fun parseRulesFromMap(yamlMap: Map<String, YamlNode>): ParseRules {
  val start: String = yamlMap.getYaml<YamlScalar>("start").content
  val productionsYaml: Map<YamlScalar, YamlNode> = yamlMap.getYaml<YamlMap>("productions").entries
  val nonterminals: Set<String> = productionsYaml.keys.map { it.content }.toSet()

  val productionsMap = productionsYaml.entries.map { entry ->
    NonTerminal(entry.key.content) to
      (entry.value as? YamlList ?: TODO()).items
        .map {
          val (name, rhsString) = when (it) {
            is YamlMap -> it.asPair()
            is YamlScalar -> null to it.content
            else -> TODO()
          }
          val rhs = rhsString
            .split("\\p{IsWhite_Space}+".toRegex())
            .filter { it.isNotEmpty() }
            .map { if (it in nonterminals) NonTerminal(it) else Terminal(it) }
          Production(NonTerminal(entry.key.content), name, rhs)
        }
        .toSet()
  }.toMap()

  return ParseRules(NonTerminal(start), productionsMap)
}

/**

whitespace: \s+
terminals:
  - STRING: '"[^"]"'
  - NUM: \d+
  - IF: if
  - (: (
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

private inline fun <reified T : YamlNode> Map<String, YamlNode>.getYaml(key: String): T =
  (this[key] ?: throw MissingRequiredPropertyException(key, YamlPath.root))
    as? T ?: throw IncorrectTypeException("Value for '${key}' is not a ${T::class.java.name}.", YamlPath.root)

private fun YamlMap.asPair(): Pair<String, String> {
  if (this.entries.size < 1) TODO()
  if (this.entries.size > 1) TODO()
  val p = this.entries.toList().single()
  return p.first.content to (p.second as? YamlScalar ?: TODO()).content
}
