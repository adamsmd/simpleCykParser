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
import kotlin.text.Regex
import kotlin.text.toRegex

/**
 * Parses a string as Yaml containing a map.
 *
 * @return the map resulting from parsing the string
 */
fun String.toYamlMap(): YamlMap = Yaml.default.parseToYamlNode(this).cast<YamlMap>()

/**
 * Extracts lexing rules from a [YamlMap].
 *
 * @return the lexing rules extracted from the [YamlMap]
 */
fun YamlMap.toLexRules(): LexRules {
  val map = this.toMap()
  val whitespace: Regex = map.getYaml<YamlScalar>("whitespace").content.toRegex()

  val terminals = map.getYaml<YamlList>("terminals").items.map {
    it.cast<YamlMap>().toPair()
  }.map { LexRule(Terminal(it.first), it.second.toRegex()) }
  // TODO: regex RegexOption.COMMENTS

  return LexRules(whitespace, terminals)
}

/**
 * Extracts parsing rules from a [YamlMap].
 *
 * @return the parsing rules extracted from the [YamlMap]
 */
fun YamlMap.toParseRules(): ParseRules {
  val map = this.toMap()
  val start: String = map.getYaml<YamlScalar>("start").content
  val productionsYaml: Map<YamlScalar, YamlNode> = map.getYaml<YamlMap>("productions").entries
  val nonterminals: Set<String> = productionsYaml.keys.map { it.content }.toSet()

  val whitespaceRegex = "\\p{IsWhite_Space}+".toRegex()
  val productionsMap = productionsYaml.entries.map { entry ->
    Nonterminal(entry.key.content) to
      entry.value.cast<YamlList>().items.map { yamlNode ->
        val (name, rhsString) = when (yamlNode) {
          is YamlMap -> yamlNode.toPair()
          is YamlScalar -> null to yamlNode.content
          else -> incorrectType(yamlNode, "YamlMap or YamlScalar")
        }
        val rhs = rhsString
          .split(whitespaceRegex)
          .filter { it.isNotEmpty() }
          .map { if (it in nonterminals) Nonterminal(it) else Terminal(it) }
        Production(Nonterminal(entry.key.content), name, rhs)
      }.toSet()
  }.toMap()

  return ParseRules(Nonterminal(start), productionsMap)
}

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
  T: []

*/

/**
 * Extracts a grammar from a [YamlMap].
 *
 * @return the grammar extracted from the [YamlMap]
 */
fun YamlMap.toGrammar(): Grammar = Grammar(this.toLexRules(), this.toParseRules())

/**********************/
/* Private Helpers */
/*********************/

private fun incorrectType(node: YamlNode, type: String): Nothing =
  throw IncorrectTypeException("Value is not a ${type}.", node.path)

private inline fun <reified T : YamlNode> YamlNode.cast(): T =
  this as? T ?: incorrectType(this, T::class.java.name)

private inline fun <reified T : YamlNode> Map<String, YamlNode>.getYaml(key: String): T =
  (this[key] ?: throw MissingRequiredPropertyException(key, YamlPath.root)).cast<T>()

private fun YamlMap.toMap(): Map<String, YamlNode> = this.entries.mapKeys { it.key.content }

private fun YamlMap.toPair(): Pair<String, String> {
  require(this.entries.size == 1) { "TODO" }
  val p = this.entries.toList().single()
  return p.first.content to p.second.cast<YamlScalar>().content
}
